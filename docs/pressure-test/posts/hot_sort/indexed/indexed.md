# HOT 排序查询性能分析

## 1. 测试目的

针对论坛首页 HOT 排序查询进行性能分析。

本次测试主要关注两个问题：

1. HOT 查询在当前数据规模下的单次执行性能。
2. 随着并发请求增加，HOT 查询是否出现明显的性能拐点。

本次实验不以“必须增加索引”为目标，而是根据实际执行计划和测试结果判断当前真正的性能瓶颈。

------

## 2. 测试条件

### 2.1 数据规模

当前数据库中约有：

| 数据           | 数量   |
| -------------- | ------ |
| posts          | 10005  |
| browse_records | 100117 |
| comments       | 50003  |

### 2.2 查询特点

HOT 排序不是直接按照某一个数据库字段排序，而是根据多个指标动态计算 `hot_score`，主要包括：

- 24 小时浏览用户数
- 点赞数量
- 评论数量
- 帖子发布时间
- 时间衰减
- 各指标归一化
- 最终 HOT 分数

最终按照：

```sql
ORDER BY hot_score DESC,
         create_time DESC,
         id DESC
```

进行排序。

------

## 3. 数据库执行计划分析

使用 `EXPLAIN ANALYZE` 对当前 HOT 查询进行分析。

当前 HOT 查询**未针对该查询额外增加索引**。

### 3.1 posts 主表

执行计划：

```text
Table scan on p
(actual time=0.00547..4.39 rows=10005 loops=1)
```

约 1 万条帖子进行全表扫描，耗时仅为数毫秒。

因此当前数据规模下，`posts` 主表扫描不是主要性能瓶颈。

------

### 3.2 browse_records

HOT 查询需要统计最近 24 小时的帖子浏览用户数：

```sql
WHERE br.target = 'POST'
  AND br.create_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
GROUP BY br.target_id
```

当前约有 10 万条浏览记录。

执行计划中：

```text
Table scan on br
(actual time=0.32..11.3 rows=100117 loops=1)
```

随后进行过滤、排序以及：

```sql
COUNT(DISTINCT br.user_id)
```

聚合。

`view_stats` 最终物化约耗时：

```text
≈ 23 ms
```

------

### 3.3 comments

HOT 查询需要统计帖子根评论数量：

```sql
WHERE c.target = 'POST'
  AND c.parent_id = 0
  AND c.is_deleted = 0
GROUP BY c.target_id
```

当前约有 5 万条评论。

执行计划中：

```text
Table scan on c
(actual time=0.241..6.54 rows=50003 loops=1)
```

随后通过临时表进行聚合：

```text
Aggregate using temporary table
(actual time=17.1..17.1 rows=10000)
```

当前评论统计耗时约：

```text
≈ 17 ms
```

------

### 3.4 窗口函数与临时表

HOT 查询中较明显的计算成本来自连续的窗口聚合。

执行过程中依次出现多个：

```text
Window aggregate with buffering
```

例如：

```text
max(base.view_log) OVER ()
actual time=85.8..91.1
```

以及：

```text
max(comment_log) OVER ()
actual time=117..123
```

以及：

```text
max(abs(signed_like_log)) OVER ()
actual time=149..155
```

期间还多次进行了临时表物化。

因此 HOT 查询的主要执行过程可以概括为：

```text
posts
 ↓
users / boards / ratings JOIN
 ↓
browse_records 聚合
 ↓
comments 聚合
 ↓
临时表
 ↓
窗口聚合
 ↓
临时表
 ↓
窗口聚合
 ↓
临时表
 ↓
窗口聚合
 ↓
hot_score 计算
 ↓
最终排序
```

------

## 4. 单次查询基线

最外层执行计划：

```text
Sort row IDs: hot_score DESC, create_time DESC, id DESC
(actual time=171..173 rows=10005 loops=1)
```

因此 `EXPLAIN ANALYZE` 观察到的数据库执行时间约为：

```text
173 ms
```

在 Navicat 中实际观察到的整体执行耗时约为：

```text
0.3 s
```

两者统计范围并不完全相同，Navicat 的整体耗时还可能包含结果集传输以及客户端处理等开销。

### 当前结论

在约 1 万条帖子、10 万条浏览记录和 5 万条评论的数据规模下：

> **HOT 查询虽然包含多次聚合、窗口函数、临时表以及动态分数计算，但单次执行耗时仍处于可接受范围。**

因此当前没有必要仅因为执行计划中存在 `Table scan` 就盲目增加索引。

------

## 5. 索引处理思路

此前针对普通帖子排序字段进行了索引实验。

`create_time`、`like_count`、`comment_count` 增加单列索引后，当前首页查询的执行计划没有发生明显变化，实际性能收益也不明显。

因此本次 HOT 查询不再采用“看到全表扫描就增加索引”的方式。

HOT 查询与普通排序不同：

```text
普通排序：

ORDER BY like_count
ORDER BY comment_count
ORDER BY create_time
```

而 HOT 排序：

```text
浏览数据
+
评论数据
+
点赞数据
+
时间衰减
+
归一化
+
窗口聚合
        ↓
动态计算 hot_score
        ↓
ORDER BY hot_score
```

`hot_score` 并不是一个已经存储在表中的固定字段，因此无法简单通过一个普通索引直接解决最终排序问题。

在当前数据规模下，单次 HOT 查询本身也没有表现出必须通过索引优化才能解决的明显瓶颈，因此暂不进行无目的的索引堆叠。

------

## 6. JMeter 并发测试

此前对 HOT 查询进行并发测试，观察到明显的性能拐点：

| 并发线程 | P95     |
| -------- | ------- |
| 1        | 686 ms  |
| 2        | 500 ms  |
| 3        | 530 ms  |
| 4        | 541 ms  |
| 5        | 812 ms  |
| 6        | 1231 ms |
| 7        | 1546 ms |
| 8        | 1766 ms |
| 9        | 2138 ms |
| 10       | 2345 ms |

其中 1 线程仅有少量样本，因此 1 → 2 线程的下降不能直接视为性能提升。

更值得关注的是：

```text
4线程 → 5线程
541ms → 812ms
```

以及：

```text
5线程 → 6线程
812ms → 1231ms
```

P95 在 5～6 线程附近出现明显增长。

因此当前可以将：

> **5～6 线程附近作为 HOT 查询的重点并发拐点区域。**

------

## 7. 当前分析

目前实验得到两个重要结果。

### 单次查询

```text
HOT SQL
≈ 173 ms
```

当前数据规模下并不算严重。

### 并发请求

```text
4线程
≈ 541 ms

5线程
≈ 812 ms

6线程
≈ 1231 ms
```

随着并发增加，响应时间出现明显增长。

因此当前更值得关注的并不是：

> “单次 HOT 查询是不是已经慢到必须增加索引？”

而是：

> **多个请求同时执行 HOT 计算时，大量相同或高度相似的计算是否被重复执行。**

------

## 8. 当前结论

当前 HOT 查询的优化结论：

1. 当前数据规模下，`posts` 全表扫描耗时较低，并不是主要瓶颈。
2. `browse_records` 和 `comments` 虽然存在全表扫描，但当前聚合耗时仍处于较低水平。
3. HOT 查询更明显的数据库计算成本来自窗口聚合、临时表以及动态 `hot_score` 计算。
4. 当前单次 HOT SQL 执行约 173ms，整体执行约 0.3s，暂未表现出必须通过索引解决的单次查询瓶颈。
5. 因此暂不盲目增加 HOT 专用索引。
6. JMeter 测试显示 HOT 在 5～6 线程附近出现明显性能拐点。
7. 当前更值得进一步研究的是高并发情况下 HOT 计算被重复执行所产生的数据库压力。

------

## 9. 后续优化方向

下一阶段可以在保持数据库查询逻辑不变的情况下，引入 Redis 对 HOT 结果进行缓存，并保持相同的 JMeter 测试条件进行对照。

实验流程：

```text
当前 HOT
    ↓
无 Redis
    ↓
记录 5～6 线程附近性能
    ↓
引入 Redis 缓存
    ↓
保持相同测试条件
    ↓
再次进行 JMeter 测试
    ↓
比较 P95、吞吐量、错误率
```

重点观察 Redis 是否能够减少重复执行 HOT 查询带来的数据库压力，以及原有的并发性能拐点是否发生变化。