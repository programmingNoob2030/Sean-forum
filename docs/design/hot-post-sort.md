# Hot Post Sort 技术设计

## 1. 设计目标

本设计为现有 `/posts` 接口补齐 `HOT` 排序逻辑。方案必须符合当前个人论坛项目规模，优先复用现有 Spring MVC、Service、Mapper、MyBatis XML、PageHelper 和 MySQL 结构。

本 Feature 不做 Redis 缓存，不改数据库结构，不引入新中间件，不实现推荐系统。

## 2. 当前代码结构分析

### 2.1 接口入口

当前帖子列表入口位于：

- `backend/src/main/java/sim/forum/controller/PostController.java`

相关方法：

- `@GetMapping("/posts")`
- `@OptionalAuth`
- `getPosts(PostQueryDTO dto)`

该方法直接调用：

- `postService.getPosts(dto)`

说明：

- `/posts` 是可选认证接口。
- 已登录时可通过 `UserContext.getUserId()` 获取当前用户 ID。
- 未登录时用户 ID 可能为空。
- 返回结构为 `Result<PageResult<PostVO>>`。

### 2.2 Service 层

当前帖子 Service 位于：

- `backend/src/main/java/sim/forum/service/PostService.java`
- `backend/src/main/java/sim/forum/service/impl/PostServiceImpl.java`

当前 `PostServiceImpl#getPosts` 行为：

1. 处理默认分页参数。
2. 调用 `PageHelper.startPage(num, size)`。
3. 调用 `postMapper.getPosts(dto, UserContext.getUserId())`。
4. 若列表为空，当前会抛出 `BusinessException("当前没有任何帖子!")`。
5. 对返回的 `PostVO` 执行 `enrichPostContent`。
6. 使用 `PageResult.of(new PageInfo<>(list))` 返回分页结果。

设计约束：

- PageHelper 必须紧贴 Mapper 查询调用。
- 热门排序必须在数据库查询阶段完成，不能先分页再在 Java 内排序。
- 内容富文本补充逻辑 `enrichPostContent` 需要保留。

### 2.3 Mapper 层

当前帖子 Mapper 位于：

- `backend/src/main/java/sim/forum/mapper/PostMapper.java`
- `backend/src/main/resources/mapper/PostMapper.xml`

`PostMapper#getPosts(PostQueryDTO dto, Long userId)` 当前负责帖子列表查询。

当前 XML 查询会：

- 从 `posts p` 查询帖子。
- 左连接 `users u` 返回发布者昵称和头像。
- 左连接 `ratings r` 返回当前用户对帖子的评分状态。
- 左连接 `boards b` 返回社区名称和封面。
- 支持 `dto.boardId` 筛选。
- 根据 `dto.sort` 选择排序：
  - `RECENT`：`p.create_time DESC`
  - `POPULAR`：`p.like_count DESC`
  - `COMMENTS`：`p.comment_count DESC`
  - `HOT`：当前仍是 `p.create_time DESC`

结论：

- `HOT` 已经存在枚举和前端入口，但后端排序只是占位逻辑。
- 本 Feature 应补齐 `HOT` 的实际排序规则。

### 2.4 DTO 与前端入口

当前排序 DTO：

- `backend/src/main/java/sim/forum/dto/post/PostQueryDTO.java`

当前 `PostSort` 枚举已经包含：

- `RECENT`
- `POPULAR`
- `COMMENTS`
- `HOT`

前端类型和首页入口当前也已经存在：

- `frontend/src/models/post/postTypes.ts`
- `frontend/src/models/post/postStore.ts`
- `frontend/src/components/index/HomeFeed.vue`

`HomeFeed.vue` 中已有“最热门”按钮，传入值为 `HOT`。

结论：

- 后续开发大概率不需要新增 sort 枚举。
- 重点是后端 `HOT` 分支的真实计算与排序。

## 3. 当前数据结构分析

### 3.1 posts 表

来源：

- `sql/forum_db.sql`
- `backend/src/main/java/sim/forum/entity/Post.java`

关键字段：

| 字段 | 说明 |
| --- | --- |
| `id` | 帖子 ID |
| `creator` | 发布者 |
| `board_id` | 所属社区 |
| `title` | 标题 |
| `content` | 正文 |
| `create_time` | 发布时间 |
| `like_count` | 当前帖子评分计数字段 |
| `comment_count` | 当前帖子评论计数字段 |
| `is_deleted` | 逻辑删除标记 |

注意：

- 当前 `like_count` 由 `ToggleRatingEvent` 通过 delta 原子更新。
- 点赞、取消、点踩会使该字段按状态差值变化。
- 因此在当前业务语义下，`posts.like_count` 更接近净点赞数，而不是单纯点赞总数。

### 3.2 ratings 表

来源：

- `sql/forum_db.sql`
- `backend/src/main/java/sim/forum/entity/Rating.java`
- `backend/src/main/java/sim/forum/service/impl/RatingServiceImpl.java`

关键字段：

| 字段 | 说明 |
| --- | --- |
| `creator` | 评分用户 |
| `target` | 评分对象类型，帖子为 `POST` |
| `target_id` | 评分对象 ID |
| `type` | 评分状态，`1` 点赞，`0` 中立，`-1` 点踩 |

当前唯一索引：

- `unique_user_like(creator, target, target_id)`

设计选择：

- 热门排序中的净点赞数优先使用 `posts.like_count`。
- 不在本 Feature 中每次通过 `ratings` 表重新聚合净点赞数。

理由：

- 现有业务已经维护帖子评分冗余计数。
- `/posts` 是高频接口，直接聚合 `ratings` 会额外增加查询压力。
- 使用现有冗余字段更符合当前项目简单、可维护的定位。

已知风险：

- 如果历史数据或事件更新曾经造成 `posts.like_count` 与 `ratings` 表不一致，热门排序会继承该不一致。
- 该问题可作为后续数据校验或修复任务，不在本 Feature 中顺手修复。

### 3.3 comments 表

来源：

- `sql/forum_db.sql`
- `backend/src/main/java/sim/forum/entity/Comment.java`
- `backend/src/main/resources/mapper/CommentMapper.xml`

关键字段：

| 字段 | 说明 |
| --- | --- |
| `target` | 评论对象类型 |
| `target_id` | 评论对象 ID |
| `root_id` | 根对象 ID |
| `root_type` | 根对象类型 |
| `parent_id` | 顶级评论 ID |
| `is_deleted` | 逻辑删除标记 |

当前根评论查询逻辑：

- `target = 'POST'`
- `target_id = #{postId}`
- `parent_id = 0`
- `is_deleted = 0`

设计选择：

- 热门排序中的顶级评论数量使用同样口径：`target='POST'`、`target_id=帖子ID`、`parent_id=0`、`is_deleted=0`。
- 不直接使用 `posts.comment_count`。

理由：

- 用户明确要求只统计顶级评论。
- 当前 `PostServiceImpl` 监听评论事件时按 `rootId` 更新 `post.comment_count`，该字段更可能表示帖子下全部评论数量，而不是顶级评论数量。

### 3.4 browse_records 表

来源：

- `sql/forum_db.sql`
- `backend/src/main/java/sim/forum/entity/BrowseRecord.java`
- `backend/src/main/java/sim/forum/service/impl/BrowseRecordServiceImpl.java`

关键字段：

| 字段 | 说明 |
| --- | --- |
| `user_id` | 浏览用户 |
| `target` | 浏览对象类型，帖子为 `POST` |
| `target_id` | 浏览对象 ID |
| `create_time` | 浏览时间 |

当前行为：

- 用户进入帖子详情时，`PostServiceImpl#getPostById` 调用 `browseRecordService.saveRecordAsync`。
- Redis 保存最近浏览 ID，用于用户最近浏览记录。
- MySQL `browse_records` 保存浏览流水。

设计选择：

- 热门排序的 24 小时浏览人数统计基于 MySQL `browse_records`。
- 不读取 Redis 最近浏览缓存。
- 不修改 `browse_records` 表结构。

已知问题：

- `browse_records` 当前 SQL 初始化脚本未发现面向热门排序的复合索引。
- 在数据量较大时，按 24 小时窗口统计 distinct 用户可能成为性能压力。
- 本 Feature 按要求不修改表结构，该问题记录为后续优化项。

## 4. 数据来源

热门排序使用以下数据：

| 指标 | 数据来源 | 统计口径 |
| --- | --- | --- |
| 近 24 小时不同用户浏览人数 | `browse_records` | `target='POST'`，`target_id=post.id`，`create_time >= 当前时间 - 24小时`，`COUNT(DISTINCT user_id)` |
| 净点赞数 | `posts.like_count` | 当前冗余计数，允许为负数 |
| 顶级评论数量 | `comments` | `target='POST'`，`target_id=post.id`，`parent_id=0`，`is_deleted=0` |
| 时间因子 | `posts.create_time` | 根据帖子年龄计算衰减 |

## 5. 热门度计算方案

### 5.1 原始指标

对每个候选帖子计算：

```text
viewers24 = 最近 24 小时不同用户浏览人数
netLike = 净点赞数
topComments = 顶级评论数量
ageHours = 当前时间与帖子发布时间的小时差
```

### 5.2 Log 压缩

浏览人数和评论数量可能有明显长尾分布。如果直接使用原始值，少数高互动帖子会压倒其他指标。

对非负数量型指标使用自然对数压缩：

```text
viewLog = ln(1 + viewers24)
commentLog = ln(1 + topComments)
```

理由：

- `ln(1 + x)` 在 `x=0` 时结果为 `0`，不会产生异常。
- 数值越大，增长越慢，可以降低极端值影响。
- 公式简单，容易在面试和维护中解释。

### 5.3 净点赞负数处理

净点赞数可能为负数，不能直接使用 `ln(1 + netLike)`。

采用带符号 Log：

```text
signedLikeLog = sign(netLike) * ln(1 + abs(netLike))
```

示例：

| netLike | signedLikeLog 含义 |
| --- | --- |
| 正数 | 正向加分 |
| 0 | 中性 |
| 负数 | 负向扣分 |

理由：

- 保留净点赞数的方向。
- 正负互动都经过 Log 压缩。
- 避免 `netLike <= -1` 时 `ln(1 + netLike)` 不合法。

### 5.4 归一化

不同指标数量级不同，压缩后仍需要归一化到可比较区间。

在当前候选集合内计算每项指标的归一化分。

#### 浏览分

```text
viewScore =
  max(viewLog) = 0 时为 0
  否则 viewLog / max(viewLog)
```

范围：`0` 到 `1`。

#### 评论分

```text
commentScore =
  max(commentLog) = 0 时为 0
  否则 commentLog / max(commentLog)
```

范围：`0` 到 `1`。

#### 净点赞分

使用候选集合内最大的绝对带符号 Log 值做缩放：

```text
maxAbsLikeLog = max(abs(signedLikeLog))

likeScore =
  maxAbsLikeLog = 0 时为 0.5
  否则 0.5 + signedLikeLog / (2 * maxAbsLikeLog)
```

范围：`0` 到 `1`。

解释：

- `0.5` 表示净点赞中性。
- 正净点赞会高于 `0.5`。
- 负净点赞会低于 `0.5`。
- 当前候选集合中最强正向净点赞接近 `1`。
- 当前候选集合中最强负向净点赞接近 `0`。

选择该方式的原因：

- 既能体现负反馈扣分，又不会产生非法 Log。
- 比简单截断负数更符合“点赞和踩相互抵消”的业务语义。
- 比复杂贝叶斯或推荐算法更适合当前项目规模。

### 5.5 时间衰减

采用半衰期为 72 小时的指数衰减：

```text
timeScore = pow(0.5, ageHours / 72)
```

含义：

- 刚发布的帖子时间分接近 `1`。
- 发布约 72 小时后，时间分约为 `0.5`。
- 发布约 144 小时后，时间分约为 `0.25`。
- 分数随时间平滑下降，但不会突然归零。

选择该方案的原因：

- 公式简单。
- 易于解释。
- 新帖天然有更高时间分。
- 老帖仍可凭近期浏览、净点赞和顶级评论参与排序，但需要足够强的互动才能抵消时间衰减。

边界处理：

- 如果 `create_time` 为空，按时间分 `0` 处理，或在实现中使用数据库默认值保证非空。
- 如果因为时钟问题出现负年龄，按 `ageHours = 0` 处理。

## 6. 最终计算公式

```text
hotScore =
  0.10 * viewScore
  + 0.25 * likeScore
  + 0.25 * commentScore
  + 0.40 * timeScore
```

排序：

```text
ORDER BY hotScore DESC, create_time DESC, id DESC
```

权重解释：

- 时间因子 `0.40`：热门列表需要保持新鲜度，避免老帖长期霸榜。
- 净点赞 `0.25`：体现帖子质量反馈，点踩可抵消点赞。
- 顶级评论 `0.25`：体现帖子引发讨论的能力，只看顶级评论避免回复楼中楼过度放大。
- 近 24 小时浏览人数 `0.10`：体现近期关注，但浏览成本低，所以权重较小。

## 7. SQL / Java 实现思路

### 7.1 推荐集成方式

推荐在 `PostServiceImpl#getPosts` 中根据 `dto.sort` 选择 Mapper 查询：

- 非 `HOT`：继续使用现有 `postMapper.getPosts(dto, userId)`。
- `HOT`：调用新增的热门查询方法，例如 `postMapper.getHotPosts(dto, userId)`。

理由：

- 热门排序 SQL 比普通排序复杂，单独方法更清晰。
- 不影响 `RECENT`、`POPULAR`、`COMMENTS` 的既有 SQL。
- 后续如果要对热门排序加缓存，更容易定位入口。

### 7.2 Mapper 方法建议

在 `PostMapper.java` 中新增热门查询方法：

```text
List<PostVO> getHotPosts(PostQueryDTO dto, Long userId)
```

在 `PostMapper.xml` 中新增对应 `<select>`。

注意：

- 方法命名可由 Development Agent 按项目风格最终确定。
- 本设计不要求新增 VO 字段暴露 `hotScore`。
- 如果为了调试短期加入 `hotScore`，不应改变最终对前端的稳定返回契约，除非另有需求。

### 7.3 SQL 计算思路

热门查询应先在数据库中计算每个候选帖子的指标和热门分数，然后按热门分数排序，最后由 PageHelper 分页。

候选帖子查询仍需要返回 `PostVO` 当前需要的字段：

- `p.*`
- `u.avatar AS creator_avatar`
- `u.name AS creator_name`
- `r.type AS post_rating_type`
- `b.name AS board_name`
- `b.cover AS board_cover`

额外指标可通过派生表或聚合子查询得到：

- `viewers24`：从 `browse_records` 聚合。
- `top_comments`：从 `comments` 聚合。
- `net_like`：使用 `IFNULL(p.like_count, 0)`。
- `age_hours`：由 `p.create_time` 与当前时间计算。

推荐使用 MySQL 8 支持的窗口函数计算候选集合内最大值：

```text
max(viewLog) over ()
max(commentLog) over ()
max(abs(signedLikeLog)) over ()
```

如果 PageHelper 对包含窗口函数或 CTE 的 SQL 生成 count 查询存在兼容问题，可以改用派生表加交叉聚合表的写法，但必须保持同一公式和排序语义。

### 7.4 不建议 Java 内排序

不建议做法：

1. 先使用 PageHelper 查询第一页。
2. 在 Java 中对第一页计算热门度。
3. 返回排序后的第一页。

原因：

- 这样只能保证页内有序，不能保证全量热门排序。
- 第二页可能存在比分页第一页更热门的帖子。
- 与用户对“热门排序”的预期不符。

如果在 Java 中计算公式，也必须先获得完整候选集合再排序再分页，但这会把大量数据加载到内存，不适合 `/posts` 高频接口。因此本设计选择数据库排序。

## 8. 与现有 sort 的集成方式

当前 `PostQueryDTO.PostSort` 已有 `HOT`，前端类型也已有 `HOT`。

后续开发只需要：

1. 保留 `RECENT`、`POPULAR`、`COMMENTS` 当前行为。
2. 将 `HOT` 从当前 `p.create_time DESC` 占位排序替换为热门排序。
3. 当 `dto.sort` 为空时，继续遵循现有默认行为；如当前没有明确默认排序，建议保持与现有 SQL 行为兼容，不在本 Feature 中额外改变。

前端当前 `HomeFeed.vue` 已有“最热门”按钮，`postStore.indexPostDTO.sort` 默认是 `RECENT`。

## 9. 分页处理

分页仍使用现有 PageHelper。

要求：

1. `PageHelper.startPage(num, size)` 必须紧贴热门 Mapper 查询。
2. 热门分数计算和 `ORDER BY hotScore DESC` 必须发生在 SQL 查询中。
3. PageHelper 应对排序后的查询结果执行分页。
4. 返回仍使用 `PageResult.of(new PageInfo<>(list))`。

边界：

- `pageNum` 为空或小于等于 0 时，继续使用当前默认值 `1`。
- `pageSize` 为空或小于等于 0 时，继续使用当前默认值 `10`。

## 10. 性能考虑

本 Feature 不引入 Redis，但热门排序 SQL 会比普通排序更重。

主要成本：

- `browse_records` 最近 24 小时 `COUNT(DISTINCT user_id)`。
- `comments` 顶级评论聚合。
- 候选集合内归一化最大值计算。
- 按计算分数排序。

当前 SQL 初始化脚本中未发现以下表的相关复合索引：

- `posts`
- `comments`
- `browse_records`

由于本 Feature 明确不修改数据库结构，索引优化不在本期实施。但需要在已知问题中记录：当数据量增大时，热门排序可能需要针对如下查询模式补充索引或缓存：

- `browse_records(target, create_time, target_id, user_id)`
- `comments(target, target_id, parent_id, is_deleted)`
- `posts(board_id, create_time, is_deleted)`

以上只是后续优化方向，不是本 Feature 的数据库变更任务。

## 11. 边界情况处理

### 11.1 没有浏览记录

- `viewers24 = 0`
- `viewLog = 0`
- `viewScore = 0`
- 不影响整体计算。

### 11.2 没有顶级评论

- `topComments = 0`
- `commentLog = 0`
- `commentScore = 0`
- 不影响整体计算。

### 11.3 净点赞为负数

- 使用 `signedLikeLog = sign(netLike) * ln(1 + abs(netLike))`。
- 归一化后低于 `0.5`。
- 参与总分时会低于中性帖子。

### 11.4 所有帖子净点赞都是 0

- `maxAbsLikeLog = 0`
- 所有帖子的 `likeScore = 0.5`
- 净点赞指标变为中性，不影响帖子之间排序差异。

### 11.5 所有帖子都没有浏览或评论

- 浏览分和评论分都为 `0`。
- 排序主要由净点赞分和时间分决定。
- 若净点赞也相同，则新帖优先。

### 11.6 空列表

当前 `PostServiceImpl#getPosts` 对空列表会抛出 `BusinessException("当前没有任何帖子!")`。

本 Feature 不强制修改该行为。若后续开发认为空列表应返回空分页，应作为单独行为修正或在开发前确认，不建议混入热门排序实现。

### 11.7 逻辑删除帖子

当前 `/posts` XML 查询未在读取到的代码中显式过滤 `p.is_deleted = 0`，前端首页存在 `!post.isDeleted` 过滤。

本 Feature 不主动改变 `/posts` 的逻辑删除返回口径。热门查询应与当前帖子列表候选口径保持一致；如果后续确认公开列表应由后端统一排除逻辑删除帖子，应作为独立修正或统一作用于所有排序方式。

顶级评论指标必须排除已删除评论。

## 12. 方案取舍

### 12.1 为什么不使用 Redis

本 Feature 的目标是先完善业务排序逻辑，后续再考虑缓存。直接引入 Redis 会把排序逻辑和缓存策略耦合，增加排查难度。

### 12.2 为什么不使用推荐算法

当前项目定位是个人学习/求职展示的单体论坛系统。热门排序需要能清楚解释，而不是追求复杂推荐效果。

### 12.3 为什么使用 Log

浏览数、评论数、点赞数容易出现长尾。Log 可以压缩极端值，让多个指标都能参与排序。

### 12.4 为什么使用带符号 Log 处理净点赞

净点赞可能为负数，普通 `ln(1 + x)` 不适用。带符号 Log 可以同时保留正负方向和压缩效果。

### 12.5 为什么使用 72 小时半衰期

72 小时能让新帖在前三天保持较好的时间分，同时不会让老帖立刻失去所有机会。相比线性衰减，指数衰减更平滑；相比复杂时间模型，又足够简单。

### 12.6 为什么数据库排序而不是 Java 排序

数据库排序可以保证分页前全局有序。Java 页内排序会导致分页结果不正确，完整加载再排序又不适合高频列表接口。

## 13. 已知问题 / 后续优化

1. `browse_records` 缺少适配热门统计的复合索引，数据量变大后可能影响性能。
2. 未来可考虑 Redis 维护近期帖子浏览 UV 或热门榜缓存，但不属于本 Feature。
3. `posts.like_count` 与 `ratings` 表可能存在历史不一致风险，本 Feature 直接复用现有冗余字段。
4. 当前 `/posts` 是否应该后端过滤逻辑删除帖子需要统一确认，本 Feature 不单独修复。
5. 热门分数不落库，查询时实时计算；当帖子量显著增长时，可考虑定时计算或缓存。

