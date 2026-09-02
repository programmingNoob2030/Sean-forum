# 首页 `create_time` 索引实验

## 1. 测试目的

验证为 `posts.create_time` 建立单列索引后，首页按发布时间排序的查询是否发生执行计划变化，以及索引是否能够降低查询中的排序开销。

## 2. 测试条件

- **数据库**：MySQL 8.0
- **测试场景**：首页帖子列表
- **筛选条件**：无 `board_id`，即全站帖子
- **排序方式**：`p.create_time DESC`
- **posts 数据量**：约 1 万行
- **测试工具**：Navicat
- **分析方式**：`EXPLAIN ANALYZE`

索引：

```sql
CREATE INDEX idx_posts_create_time
ON posts(create_time);
```

## 3. 测试结果

### 建立索引前

执行计划：

```text
-> Sort row IDs: p.create_time
   (actual time=17.9..18.7 rows=10005 loops=1)
    -> Table scan on <temporary>
       (actual time=14.5..15.6 rows=10005 loops=1)
        -> Temporary table
           (actual time=14.5..14.5 rows=10005 loops=1)
            -> Left hash join
                ...
                -> Table scan on p
                   (actual time=0.00909..3.9 rows=10005 loops=1)
```

主要现象：

- `posts` 仍然采用全表扫描。
- 查询产生 Temporary Table。
- 最终仍需要对结果进行排序。
- `posts` 全表扫描实际耗时约 **3.9 ms**。
- 最终排序阶段约 **17.9～18.7 ms**。

### 建立索引后

执行计划：

```text
-> Sort row IDs: p.create_time
   (actual time=13.5..14.3 rows=10005 loops=1)
    -> Table scan on <temporary>
       (actual time=11.6..12.6 rows=10005 loops=1)
        -> Temporary table
           (actual time=11.6..11.6 rows=10005 loops=1)
            -> Left hash join
                ...
                -> Table scan on p
                   (actual time=0.00763..3.73 rows=10005 loops=1)
```

主要现象：

- `posts` 仍然采用全表扫描。
- Temporary Table 仍然存在。
- 最终排序仍然存在。
- `posts` 全表扫描实际耗时约 **3.73 ms**。
- 最终排序阶段约 **13.5～14.3 ms**。
- **执行计划未发生明显变化。**

## 4. 结果分析

建立 `create_time` 单列索引后，MySQL 并没有选择通过该索引直接完成最终排序。

当前查询仍然采用：

```text
posts 全表扫描
    ↓
LEFT JOIN users
    ↓
LEFT JOIN boards
    ↓
Temporary Table
    ↓
排序
```

因此，该索引暂时没有改变当前查询的主要执行路径。

虽然两次 `EXPLAIN ANALYZE` 的实际耗时存在差异，但由于执行计划没有发生变化，不能直接将这部分耗时下降归因于 `create_time` 索引。

当前数据量约为 1 万行，MySQL 可能认为直接扫描 `posts` 并完成后续 JOIN、临时表和排序的整体成本更低，因此没有选择 `create_time` 索引。

## 5. 当前结论

在当前数据规模和首页查询结构下：

> **`posts.create_time` 单列索引暂未改变首页查询的执行计划。**

查询仍存在：

- `Table scan on p`
- `Temporary table`
- 最终排序

因此，目前无法仅凭本次 `EXPLAIN ANALYZE` 证明该索引对该查询具有实际优化效果。

后续需要结合 JMeter 在相同测试条件下的 P95、吞吐量等指标进一步验证。

## 6. 后续实验

继续对首页其他排序字段进行相同实验：

- `like_count`
- `comment_count`

保持查询条件和测试方法一致，分别比较建立索引前后的：

1. `EXPLAIN ANALYZE` 执行计划
2. 实际执行耗时
3. JMeter P95
4. 吞吐量
5. 错误率

最终根据实际测试结果判断索引是否具有优化价值。