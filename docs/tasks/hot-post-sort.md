# Hot Post Sort 任务拆分

## 1. 执行边界

本任务清单供后续 Development Agent 使用。开发前必须阅读：

- `docs/requirements/hot-post-sort.md`
- `docs/design/hot-post-sort.md`
- `docs/project.md`
- `docs/architecture.md`

本 Feature 只实现 `/posts` 的 `HOT` 排序能力。

禁止在本 Feature 中顺手实现：

- Redis 缓存
- 数据库表结构变更
- browse_records 表结构调整
- 推荐系统
- Elasticsearch
- 前端重构
- 删除口径统一修复

开发前还需要执行 `git status --short`，确认并保护当前工作区中已有的用户改动。

## 2. 任务一：确认现有查询入口和 HOT 占位逻辑

### 任务目标

确认当前 `/posts` 查询链路和已有 `HOT` sort 入口，避免重复新增枚举或前端入口。

### 涉及文件 / 模块

- `backend/src/main/java/sim/forum/controller/PostController.java`
- `backend/src/main/java/sim/forum/service/PostService.java`
- `backend/src/main/java/sim/forum/service/impl/PostServiceImpl.java`
- `backend/src/main/java/sim/forum/dto/post/PostQueryDTO.java`
- `backend/src/main/java/sim/forum/mapper/PostMapper.java`
- `backend/src/main/resources/mapper/PostMapper.xml`
- `frontend/src/models/post/postTypes.ts`
- `frontend/src/models/post/postStore.ts`
- `frontend/src/components/index/HomeFeed.vue`

### 需要完成的修改

- 不需要新增 `PostSort.HOT`，因为当前代码已存在。
- 不需要新增首页“最热门”按钮，当前前端已存在。
- 记录当前 `PostMapper.xml` 中 `HOT` 仍为 `ORDER BY p.create_time DESC` 的占位实现。

### 依赖关系

无。

### 完成标准

- Development Agent 明确后续工作是替换 HOT 的后端排序逻辑，而不是新增一套排序参数体系。

## 3. 任务二：新增热门帖子 Mapper 查询

### 任务目标

在查询层实现热门排序所需的数据聚合、分数计算和排序。

### 涉及文件 / 模块

- `backend/src/main/java/sim/forum/mapper/PostMapper.java`
- `backend/src/main/resources/mapper/PostMapper.xml`

### 需要完成的修改

1. 在 `PostMapper.java` 中新增热门帖子查询方法，例如：
   - `List<PostVO> getHotPosts(PostQueryDTO dto, Long userId)`
2. 在 `PostMapper.xml` 中新增对应 `<select>`。
3. 查询返回字段需保持与现有 `getPosts` 的 `PostVO` 字段兼容：
   - `p.*`
   - `u.avatar AS creator_avatar`
   - `u.name AS creator_name`
   - `r.type AS post_rating_type`
   - `b.name AS board_name`
   - `b.cover AS board_cover`
4. 支持 `dto.boardId` 过滤，并与当前 `/posts` 候选范围保持一致。
5. 计算以下原始指标：
   - `viewers24`：最近 24 小时不同浏览用户数。
   - `netLike`：`IFNULL(p.like_count, 0)`。
   - `topComments`：未删除顶级评论数。
   - `ageHours`：帖子发布时间距离当前时间的小时数。
6. 按设计文档计算：
   - `viewScore`
   - `likeScore`
   - `commentScore`
   - `timeScore`
   - `hotScore`
7. SQL 排序必须为：
   - `hotScore DESC`
   - `p.create_time DESC`
   - `p.id DESC`

### 依赖关系

- 依赖设计文档中的最终公式。
- 依赖当前 MySQL 8 能力。

### 完成标准

- `sort=HOT` 的查询能在数据库层得到全局热门排序。
- 未登录用户 `userId = null` 时，当前用户评分状态左连接不应导致异常。
- 不改变 `PostVO` 的稳定返回结构。

## 4. 任务三：在 Service 层集成 HOT 分支

### 任务目标

让 `/posts?sort=HOT` 使用新的热门查询，同时保持其他排序方式不变。

### 涉及文件 / 模块

- `backend/src/main/java/sim/forum/service/impl/PostServiceImpl.java`
- `backend/src/main/java/sim/forum/service/PostService.java`

### 需要完成的修改

1. 在 `PostServiceImpl#getPosts` 中保留当前分页参数默认值处理。
2. 保持 `PageHelper.startPage(num, size)` 紧贴实际 Mapper 查询。
3. 当 `dto.getSort() == PostQueryDTO.PostSort.HOT` 时调用热门查询方法。
4. 其他排序继续调用现有 `postMapper.getPosts(dto, UserContext.getUserId())`。
5. 查询完成后继续执行现有 `enrichPostContent`。
6. 返回仍使用 `PageResult.of(new PageInfo<>(list))`。

### 依赖关系

- 依赖任务二完成新的 Mapper 查询。

### 完成标准

- `RECENT`、`POPULAR`、`COMMENTS` 行为不受影响。
- `HOT` 走新的热门排序查询。
- 分页仍是对 SQL 排序结果分页，而不是 Java 页内排序。

## 5. 任务四：实现边界情况处理

### 任务目标

保证热门排序在低数据量、无互动、负净点赞等场景下稳定运行。

### 涉及文件 / 模块

- `backend/src/main/resources/mapper/PostMapper.xml`
- `backend/src/main/java/sim/forum/service/impl/PostServiceImpl.java`

### 需要完成的修改

1. 浏览人数为空时按 `0` 处理。
2. 顶级评论数为空时按 `0` 处理。
3. 净点赞为空时按 `0` 处理。
4. 净点赞为负数时使用带符号 Log 处理。
5. 所有帖子某个指标最大值为 `0` 时，避免除零：
   - 浏览分为 `0`
   - 评论分为 `0`
   - 点赞分为 `0.5`
6. 帖子年龄为负数时按 `0` 小时处理。
7. 顶级评论统计必须排除 `is_deleted = 1` 的评论。
8. 顶级评论统计必须只计算 `target='POST'`、`parent_id=0` 的评论。

### 依赖关系

- 依赖任务二的 SQL 结构。

### 完成标准

- 无浏览、无评论、无点赞的帖子不会导致 SQL 异常。
- 净点赞为负数不会导致 Log 计算异常。
- 热门排序结果稳定，可重复查询。

## 6. 任务五：保留现有 sort 和分页兼容性

### 任务目标

确保热门排序不会破坏已有 `/posts` 能力。

### 涉及文件 / 模块

- `backend/src/main/resources/mapper/PostMapper.xml`
- `backend/src/main/java/sim/forum/service/impl/PostServiceImpl.java`
- `frontend/src/models/post/postStore.ts`
- `frontend/src/components/index/HomeFeed.vue`

### 需要完成的修改

1. 不修改 `RECENT`、`POPULAR`、`COMMENTS` 的排序语义。
2. 不修改 `/posts` 的返回包装结构。
3. 不修改 `PageRequestDTO` 的默认分页语义。
4. 如果前端当前已经传入 `HOT`，不做无关前端改动。
5. 如果实现后发现前端切换排序不重置页码，可另行记录问题；本 Feature 不强制修复，除非影响 `HOT` 基本验收。

### 依赖关系

- 依赖任务三完成 Service 集成。

### 完成标准

- 旧排序方式可继续使用。
- `HOT` 可通过现有前端入口或接口参数触发。
- 分页返回结构与原接口一致。

## 7. 任务六：测试准备

### 任务目标

为后续开发完成后的验证准备覆盖场景。Development Agent 完成开发后应执行相关测试或至少提供可复现验证说明。

### 涉及文件 / 模块

- 后端测试目录，按项目现有测试结构选择。
- `backend/src/main/resources/mapper/PostMapper.xml`
- `backend/src/main/java/sim/forum/service/impl/PostServiceImpl.java`

### 需要准备的测试场景

1. `sort=HOT` 能返回帖子列表。
2. 热门排序按 `hotScore DESC, create_time DESC, id DESC` 生效。
3. `boardId` 存在时只在该社区范围内排序。
4. 最近 24 小时内同一用户多次浏览同一帖子只计一次。
5. 超过 24 小时的浏览记录不计入 `viewers24`。
6. 顶级评论计入 `topComments`。
7. 回复评论不计入 `topComments`。
8. 已删除评论不计入 `topComments`。
9. 净点赞为负数时不会计算异常，且低于相同条件下净点赞为零的帖子。
10. 所有互动指标为零时仍能稳定返回结果。
11. 未登录访问 `sort=HOT` 不因 `userId` 为空异常。
12. `RECENT`、`POPULAR`、`COMMENTS` 仍可正常查询。

### 依赖关系

- 依赖任务二、三、四、五完成。

### 完成标准

- 有明确测试用例或手工验证数据集。
- 开发完成后能说明执行了哪些测试、结果如何、哪些测试未执行以及原因。

## 8. 开发顺序建议

1. 完成任务一，确认当前入口。
2. 完成任务二，新增热门查询。
3. 完成任务三，Service 层接入。
4. 完成任务四，补齐边界处理。
5. 完成任务五，确认兼容性。
6. 完成任务六，补充并执行验证。

不要在任务执行过程中增加新的业务目标。如发现需求文档和设计文档冲突，应停止并向用户说明冲突点。

