# 首页 `like_count` 索引实验

## 1. 测试目的

验证为 `posts.like_count` 建立单列索引后，首页按点赞数排序的查询是否发生执行计划变化，以及索引是否能够降低查询中的排序开销。

## 2. 测试条件

- **数据库**：MySQL 8.0
- **测试场景**：首页帖子列表
- **筛选条件**：无 `board_id`，即全站帖子
- **排序方式**：`p.like_count DESC`
- **posts 数据量**：约 1 万行
- **测试工具**：Navicat
- **分析方式**：`EXPLAIN ANALYZE`

建立索引：

```sql
CREATE INDEX idx_posts_like_count
ON posts(like_count);
```

## 3. 测试结果

### 建立索引前

核心执行过程：

```text
Sort row IDs: p.like_count
(actual time=14.3..15.8 rows=10005 loops=1)

Table scan on <temporary>
(actual time=11.7..12.8 rows=10005 loops=1)

Temporary table
(actual time=11.7..11.7 rows=10005 loops=1)

Table scan on p
(actual time=0.00946..3.85 rows=10005 loops=1)
```

主要现象：

- `posts` 采用全表扫描。
- 产生 Temporary Table。
- 最终需要对临时结果进行排序。
- `posts` 全表扫描实际耗时约 **3.85 ms**。
- 最终排序阶段约 **14.3～15.8 ms**。

### 建立索引后

核心执行过程：

```text
Sort row IDs: p.like_count
(actual time=13..14.2 rows=10005 loops=1)

Table scan on <temporary>
(actual time=10.9..11.9 rows=10005 loops=1)

Temporary table
(actual time=10.9..10.9 rows=10005 loops=1)

Table scan on p
(actual time=0.00572..3.8 rows=10005 loops=1)
```

主要现象：

- `posts` 仍采用全表扫描。
- Temporary Table 仍然存在。
- 最终排序仍然存在。
- `posts` 全表扫描实际耗时约 **3.80 ms**。
- 最终排序阶段约 **13.0～14.2 ms**。
- **执行计划没有发生明显变化。**

## 4. 结果分析

建立 `like_count` 单列索引后，MySQL 没有选择通过该索引直接完成 `ORDER BY p.like_count`。

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
按 like_count 排序
```

因此，索引没有改变当前查询的主要执行路径。

虽然建立索引后：

- 排序阶段约从 **14.3～15.8 ms** 变为 **13.0～14.2 ms**
- `posts` 扫描阶段约从 **3.85 ms** 变为 **3.80 ms**

但由于执行计划没有发生改变，因此不能直接认为这些耗时变化由索引带来。

当前 `posts` 仅约 1 万行，在这一数据规模下，全表扫描本身成本较低。与此同时，查询还需要完成多表 JOIN 并构造 Temporary Table，优化器没有选择通过 `like_count` 索引改变整体执行路径。

## 5. 当前结论

在当前数据规模和首页查询结构下：

> **`posts.like_count` 单列索引暂未改变首页查询的执行计划。**

当前查询仍然存在：

- `Table scan on p`
- `Temporary table`
- 最终排序

因此，本次 `EXPLAIN ANALYZE` **无法证明 `like_count` 索引对该查询具有实际优化效果**。

排序阶段虽然出现了耗时下降，但由于执行计划未改变，暂时应视为单次执行的波动，而不能直接归因于索引。

## 6. 后续实验

继续对首页其他排序字段进行相同实验：

- `comment_count`
- `create_time`（已完成）

并在后续 JMeter 测试中保持相同测试条件，对比：

1. `EXPLAIN ANALYZE` 执行计划
2. 实际执行耗时
3. P95
4. 吞吐量
5. 错误率

最终根据实际测试数据判断各索引是否具有优化价值。