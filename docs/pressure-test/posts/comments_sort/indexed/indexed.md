# 首页 `comment_count` 索引实验

## 1. 测试目的

验证为 `posts.comment_count` 建立单列索引后，首页按评论数排序的查询是否发生执行计划变化，以及该索引是否能够降低查询中的排序开销。

## 2. 测试条件

- **数据库**：MySQL 8.0
- **测试场景**：首页帖子列表
- **筛选条件**：无 `board_id`，即全站帖子
- **排序方式**：`p.comment_count DESC`
- **posts 数据量**：约 1 万行
- **测试工具**：Navicat
- **分析方式**：`EXPLAIN ANALYZE`

建立索引：

```sql
CREATE INDEX idx_posts_comment_count
ON posts(comment_count);
```

## 3. 测试结果

### 建立索引前

核心执行过程：

```text
Sort row IDs: p.comment_count
(actual time=12.5..13.2 rows=10005 loops=1)

Table scan on <temporary>
(actual time=10.7..11.8 rows=10005 loops=1)

Temporary table
(actual time=10.7..10.7 rows=10005 loops=1)

Table scan on p
(actual time=0.00524..3.73 rows=10005 loops=1)
```

主要现象：

- `posts` 采用全表扫描。
- 产生 Temporary Table。
- 最终需要对临时结果进行排序。
- `posts` 全表扫描实际耗时约 **3.73 ms**。
- 最终排序阶段约 **12.5～13.2 ms**。

### 建立索引后

核心执行过程：

```text
Sort row IDs: p.comment_count
(actual time=12.9..13.5 rows=10005 loops=1)

Table scan on <temporary>
(actual time=11.1..12.2 rows=10005 loops=1)

Temporary table
(actual time=11.1..11.1 rows=10005 loops=1)

Table scan on p
(actual time=0.00544..3.79 rows=10005 loops=1)
```

主要现象：

- `posts` 仍采用全表扫描。
- Temporary Table 仍然存在。
- 最终排序仍然存在。
- `posts` 全表扫描实际耗时约 **3.79 ms**。
- 最终排序阶段约 **12.9～13.5 ms**。
- **执行计划没有发生明显变化。**

## 4. 结果分析

建立 `comment_count` 单列索引后，MySQL 没有选择通过该索引直接完成 `ORDER BY p.comment_count`。

查询仍然采用：

```text
posts 全表扫描
    ↓
LEFT JOIN users
    ↓
LEFT JOIN boards
    ↓
Temporary Table
    ↓
按 comment_count 排序
```

因此，该索引没有改变当前查询的主要执行路径。

本次测试中：

- `posts` 扫描阶段：约 **3.73 ms → 3.79 ms**
- 排序阶段：约 **12.5～13.2 ms → 12.9～13.5 ms**

建立索引后单次执行耗时略有增加，但差异较小。

由于执行计划没有发生变化，且 `EXPLAIN ANALYZE` 单次执行存在运行时波动，因此不能将这次耗时变化直接归因于索引。

## 5. 当前结论

在当前数据规模和首页查询结构下：

> **`posts.comment_count` 单列索引暂未改变首页查询的执行计划。**

查询仍然存在：

- `Table scan on p`
- `Temporary table`
- 最终排序

因此，本次实验无法证明 `comment_count` 单列索引对当前首页查询具有实际优化效果。

结合此前 `create_time` 和 `like_count` 的实验，目前三个首页普通排序字段的单列索引均未改变查询执行计划。

## 6. 后续实验

下一阶段将结合 JMeter 对建立索引前后的接口性能进行测试，在相同测试条件下比较：

1. P95
2. 吞吐量
3. 错误率
4. 并发能力

并结合 `EXPLAIN ANALYZE` 的执行计划变化，判断这些索引在当前数据规模和实际并发场景下是否具有优化价值。