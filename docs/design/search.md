# 帖子搜索功能 V1 技术设计

## 1. 设计目标与现状差异

本 Feature 在现有帖子列表能力上增加可选 `keyword` 筛选，并新增搜索结果路由页面。实现优先复用已有 `/posts`、`PostVO`、PageHelper、MyBatis XML、Pinia 和 Vue Router，不引入独立搜索接口或新基础设施。

本设计以当前实际代码为准。当前项目文档描述了基本的帖子列表和 Vue Router 架构，但实际代码存在以下需要特别注意的现状：

1. `PostQueryDTO` 当前只有 `boardId` 和 `sort`，还没有 `keyword`。
2. `PostController#getPosts` 已直接接收 `PostQueryDTO`，不需要新增 Controller 路径。
3. `PostMapper.xml` 当前已有独立 `getHotPosts` 查询，不应为了搜索统一重构普通查询和 HOT 查询。
4. `PostServiceImpl` 当前会根据 `HOT` 选择 `getHotPosts`，其他排序选择 `getPosts`。
5. `PostServiceImpl` 当前存在 10 秒 Redis 帖子列表缓存，缓存 key 当前只包含 `sort`，没有包含 `keyword`、分页参数或 `boardId`。加入搜索后必须避免不同关键词共享同一个缓存结果。
6. `NavBar.vue` 当前通过 `modelValue` 与 `ForumIndex.vue` 双向绑定搜索文本，但输入框没有 Enter 搜索处理。
7. `ForumIndex.vue` 当前只注册首页及其子页面，Router 中还没有 `/search` 页面。
8. `HomeFeed.vue` 当前有本地 `searchQuery`，但只对已加载帖子标题做前端过滤，且正文不参与过滤；V1 搜索应使用独立搜索页面和后端 `keyword`，不能把该本地过滤当作正式搜索实现。
9. 前端现有 `apiGetPosts` 已封装 `/posts`，`GetPostsDTO` 需要增加可选 `keyword` 类型字段。

## 2. 当前代码链路分析

### 2.1 后端链路

当前实际链路：

```text
GET /posts
  -> PostController.getPosts(PostQueryDTO dto)
  -> PostServiceImpl.getPosts(dto)
  -> PageHelper.startPage(pageNum, pageSize)
  -> PostMapper.getPosts(...) 或 PostMapper.getHotPosts(...)
  -> PostMapper.xml
  -> MySQL
  -> PageInfo
  -> PageResult<PostVO>
```

Controller 使用 `@OptionalAuth`，因此搜索结果页可以支持未登录访问。用户评分状态仍通过当前用户 ID 左连接获取；未登录时应继续沿用当前空用户 ID 行为。

### 2.2 前端链路

当前实际页面结构：

```text
ForumIndex.vue
  -> NavBar.vue
  -> router-view
       -> HomeFeed.vue / PostDetail.vue
```

`ForumIndex.vue` 位于现有外壳中，导航栏和左侧栏位于外层，`router-view` 用于切换首页子页面。搜索页面建议作为顶层 `/search` 路由，使用独立页面组件；这样可以保留全局导航栏的存在，同时不把搜索结果错误地塞进 `HomeFeed` 的本地过滤逻辑。

## 3. 前端设计

### 3.1 NavBar 搜索框

涉及：

- `frontend/src/components/index/NavBar.vue`
- `frontend/src/views/ForumIndex.vue`

设计：

1. 保留现有 `el-input` UI、`v-model` 和清除能力。
2. 在输入框上监听 Enter 事件。
3. Enter 事件触发时读取当前搜索文本。
4. 对关键词做前端首尾空白清理。
5. 有效关键词通过 Router 跳转到 `/search`，使用 query 参数 `keyword`。
6. 通过 Router 的 query 编码机制传递关键词，不手工拼接未编码的 URL。
7. 空或纯空格关键词按“无有效搜索条件”处理，推荐跳转到 `/search` 并去掉 `keyword` query，或跳转首页；最终实现需与需求中的空关键词口径一致。

`ForumIndex.vue` 现有的 `searchQuery` 只用于导航栏双向绑定。为了让搜索结果页也能直接使用同一个导航栏，建议不把搜索请求放到 `ForumIndex.vue` 中，而由 `NavBar.vue` 负责发出路由导航。

### 3.2 Router

涉及：

- `frontend/src/router/index.ts`

新增独立路由：

```text
/search?keyword=xxx
```

建议使用懒加载的页面组件，例如 `frontend/src/views/search/Search.vue`，但具体文件命名可以按当前 views 风格确定。页面不使用 `window.open()`，不创建新窗口。

搜索路由应支持：

- 直接通过 URL 打开。
- 刷新后根据 query 恢复关键词。
- 关键词变更时触发结果重新加载。

### 3.3 搜索结果页面

建议新增：

- `frontend/src/views/search/Search.vue`

页面职责：

1. 从 `route.query.keyword` 读取关键词。
2. 将 query 参数转换为字符串并进行首尾空白处理。
3. 维护当前页码、每页数量、排序方式、帖子列表和总数量。
4. 调用已有 `apiGetPosts`，请求参数包含 `keyword`、`pageNum`、`pageSize` 和 `sort`。
5. 复用 `PostCard` 展示 `PostVO`。
6. 提供现有排序选项，至少保证四个 sort 值可以与搜索组合。
7. 使用 `el-pagination` 展示总数并响应页码变化。
8. 搜索页面请求失败时复用项目现有错误处理或提示方式。
9. 无结果时显示空状态，不把无结果误显示为全量帖子。

排序默认值建议使用 `RECENT`，与首页 `indexPostDTO` 当前默认值一致。排序切换时应将页码重置为 `1`，但这只是搜索结果页内部状态变化，不改变 URL 中的 keyword。

### 3.4 翻页和关键词变化

翻页：

```text
当前 keyword 保持不变
pageNum 更新
重新调用 apiGetPosts
```

关键词变化：

```text
NavBar 按 Enter
Router query.keyword 更新
Search 页面观察 route query 变化
pageNum 重置为 1
重新调用 apiGetPosts
```

需要避免：

- 只改本地列表而不请求后端。
- 翻页丢失 keyword。
- 关键词变化后继续使用旧页码。
- 用户在当前 `/search?keyword=a` 再搜索 `b` 时组件未重新加载数据。

## 4. 后端设计

### 4.1 PostQueryDTO

涉及：

- `backend/src/main/java/sim/forum/dto/post/PostQueryDTO.java`

新增可选字段：

```text
private String keyword;
```

不新增独立 Search DTO，因为搜索本质上是帖子列表的一个可选筛选条件。

参数建议：

- `null`：无搜索条件。
- 空字符串：无搜索条件。
- 首尾空格：Service 层统一 trim 后判断；全为空格时无搜索条件。
- 非空关键词：传给 Mapper 做标题/正文包含匹配。

### 4.2 Controller

涉及：

- `backend/src/main/java/sim/forum/controller/PostController.java`

不需要新增 Controller 方法或接口。现有 `getPosts(PostQueryDTO dto)` 会由 Spring 将 query 参数绑定到新增 `keyword` 字段。

只有在需要统一参数校验或显式规范化时才调整 Controller；优先在 Service/DTO 处理，避免扩大 Controller 改动范围。

### 4.3 Service

涉及：

- `backend/src/main/java/sim/forum/service/impl/PostServiceImpl.java`

Service 需要完成两类职责：

1. 规范化 `keyword` 的 null、空字符串和首尾空格。
2. 保证普通查询和 HOT 查询都接收到同一个有效关键词。

建议在进入查询前对 DTO 的关键词进行 trim；如果项目不希望修改请求 DTO 对象，也可以使用局部规范化值，但必须保证 XML 判断条件可区分空关键词。

当前 `queryPosts` 已按 `HOT` 分流：

```text
HOT -> postMapper.getHotPosts(dto, userId)
其他 -> postMapper.getPosts(dto, userId)
```

该分流应保留，不要为了搜索统一成一个大型查询。

### 4.4 Redis 缓存兼容

当前 `PostServiceImpl#getPosts` 存在帖子列表 Redis 缓存：

```text
key = dto.getSort().name() + ":sort:posts"
```

这段实际代码存在两个与搜索相关的风险：

1. key 没有 keyword，搜索 `redis` 和搜索 `java` 可能读到同一缓存。
2. key 没有 pageNum、pageSize、boardId，当前缓存本身也不能准确表达完整查询条件。

本 Feature 的需求明确不包含 Redis 搜索缓存，但实际代码已经使用 Redis，因此开发时不能忽略现有缓存的适用范围。

当前现有 Redis 列表缓存的设计意图是缓存**全站帖子列表的第一页**。由于缓存 key 仅包含 `sort`，未包含 `pageNum`、`pageSize`、`boardId`、`keyword` 等查询条件，开发时应限制现有缓存的使用范围：

- 仅当 `pageNum = 1`、`boardId` 为空且无有效 `keyword` 时使用现有列表缓存；
- 其他请求直接查询数据库，不读取或写入现有列表缓存；
- 因此，带有效 `keyword` 的搜索请求不会复用现有列表缓存，也不会将搜索结果写入现有列表缓存。

该处理方式不新增 Redis 搜索缓存，也不修改现有 Redis key 结构，仅明确现有首页缓存的使用边界，避免不同查询条件错误共用同一缓存。

此外，当前代码在 `PageHelper.startPage` 后读取 Redis，命中缓存时 PageHelper 不会真正作用于缓存列表，可能导致分页语义问题。此次开发应确保非首页缓存场景（包括搜索、指定 `boardId` 或非第一页请求）直接执行数据库查询，从而保证分页和查询条件正确。

## 5. SQL 设计

### 5.1 普通排序：RECENT / POPULAR / COMMENTS

普通查询位于：

- `backend/src/main/resources/mapper/PostMapper.xml`
- `<select id="getPosts">`

当前查询已通过 `PostBaseWhere` 处理 `boardId`，并通过 choose 处理排序。应在同一个候选帖子过滤区域增加可选搜索条件：

```text
keyword 有效时：
(
  p.title LIKE 包含 keyword
  OR p.content LIKE 包含 keyword
)
```

关键要求：

1. 搜索条件与现有 `boardId` 条件通过 AND 连接。
2. 标题与正文条件必须用括号包裹，避免 OR 改变 `boardId` 条件的逻辑。
3. keyword 为空时不生成该条件，查询结果与原有行为一致。
4. 保留现有排序：
   - `POPULAR`：`p.like_count DESC`
   - `COMMENTS`：`p.comment_count DESC`
   - 其他默认：`p.create_time DESC`
5. 不增加搜索权重，不按标题命中优先排序。

MyBatis 参数建议使用 `#{}` 绑定参数，并使用 XML 的条件判断和字符串拼接能力构造包含匹配；不得把用户输入直接拼入 SQL 文本。

### 5.2 HOT 独立查询

当前 HOT 查询位于：

- `backend/src/main/resources/mapper/PostMapper.xml`
- `<select id="getHotPosts">`

HOT 使用独立的 SQL 查询，不与普通帖子排序查询共用完整查询逻辑。

搜索适配时需要注意：

1. 当存在有效 `keyword` 时，HOT 查询也必须按照 `title` 或 `content` 过滤帖子。
2. `keyword` 和现有 `boardId` 条件都应在 HOT 查询的帖子筛选阶段生效，而不是在 HOT 结果计算完成后再过滤。
3. HOT 的现有评分计算、排序方式和分页行为保持不变。
4. 不重构 HOT 的评分逻辑，也不将 HOT 查询与普通查询强行合并。
5. 优先复用当前已有的 `PostListWhere` 条件；如果修改公共条件会导致 HOT 查询难以维护，则在 HOT 查询中增加必要的 `keyword` 条件即可。

核心要求只有一点：

> **HOT 搜索应先筛选出符合 `keyword` 和 `boardId` 条件的帖子，再按照现有 HOT 逻辑计算和排序。**

### 5.3 大小写行为

项目 SQL 使用的 MySQL 字符集排序规则为 `utf8mb4_0900_ai_ci`，其中 `ci` 通常表示大小写不敏感。V1 应依赖当前数据库排序规则实现不区分大小写，不额外加入复杂全文搜索或分词逻辑。

如果运行环境排序规则与初始化脚本不同，大小写行为可能变化，应在联调或测试准备中确认；本 Feature 不引入数据库结构调整。

### 5.4 逻辑删除口径

当前普通 `/posts` 查询实际读取的 XML 没有显式 `p.is_deleted = 0`，首页 `HomeFeed.vue` 通过 `!post.isDeleted` 做前端过滤。HOT 查询使用相同的帖子候选 where 片段。

本 Feature 不顺手修复逻辑删除口径。搜索应与当前 `/posts` 的既有候选口径保持一致；若后续要统一由后端排除逻辑删除帖子，应另行处理并覆盖所有排序方式。

## 6. 分页设计

后端继续使用：

- `PageHelper.startPage(num, size)`
- Mapper 查询
- `PageInfo<>(list)`
- `PageResult.of(...)`

要求：

1. 搜索条件必须在 SQL 中完成。
2. SQL 过滤和排序完成后，再由 PageHelper 分页。
3. 普通排序和 HOT 排序均不能先查出当前页再在 Java 过滤。
4. 搜索结果返回的 `total` 必须是关键词匹配结果总数。
5. 前端翻页请求必须携带当前 keyword。
6. 前端关键词变化时 pageNum 必须重置为 `1`。

由于当前 Service 的缓存路径可能绕过 PageHelper，带有效 keyword 的查询推荐直接走数据库查询，以确保分页 count 和 list 正确。

## 7. 参数处理

### 7.1 null、空字符串和空格

建议统一规则：

| 输入 | 处理 |
| --- | --- |
| `null` | 无搜索条件 |
| `""` | 无搜索条件 |
| `"   "` | trim 后为空，无搜索条件 |
| `" redis "` | trim 为 `redis` 后搜索 |
| `"redis"` | 搜索 `redis` |

这样可以避免用户只输入空格时生成 `LIKE '%%'` 的无意义条件。

### 7.2 特殊字符

V1 使用 `LIKE` 包含匹配。`%` 和 `_` 在 LIKE 中具有通配符语义，反斜杠可能参与转义。建议按以下简单口径处理：

- 使用 MyBatis 参数绑定，禁止字符串直接拼接 SQL。
- 对用户输入的 LIKE 特殊字符进行转义，使其按普通关键词字符匹配；同时指定相应 LIKE 转义规则。
- 不因为普通关键词内容改变 SQL 结构。

这一处理避免用户输入 `%` 后意外匹配全部帖子，也避免 SQL 注入风险。转义细节属于 SQL 实现工作，但不得改变 V1 的“简单包含搜索”目标。

### 7.3 超长 keyword

建议 V1 将有效 keyword 限制为最多 `100` 个字符：

- 超过限制时由后端拒绝并返回明确参数错误，或由前端限制输入长度并由后端再次校验。
- 具体错误响应沿用项目现有异常和统一返回机制。
- 不新增独立错误格式。

该限制是设计建议，对应需求文档中的待确认项；若产品确认其他长度，应只同步修改三份文档和实现口径。

## 8. 数据流

```text
NavBar.vue
  -> 用户按 Enter
  -> Vue Router.push({ name/path: 'Search', query: { keyword } })
  -> Search 页面读取 route.query.keyword
  -> apiGetPosts({ keyword, pageNum, pageSize, sort })
  -> GET /posts
  -> PostController.getPosts(PostQueryDTO)
  -> PostServiceImpl.getPosts(dto)
  -> 规范化 keyword
  -> HOT ? PostMapper.getHotPosts : PostMapper.getPosts
  -> PostMapper.xml 可选 keyword 条件
  -> MySQL 标题/正文 LIKE 过滤
  -> SQL 排序
  -> PageHelper 分页
  -> PageResult<PostVO>
  -> Search 页面更新列表、总数和分页状态
```

## 9. PostVO 复用

当前 `PostVO` 继承 `Post`，并包含发布者、板块、评分状态以及帖子内容展示字段。`PostMapper.xml` 普通查询和 HOT 查询都已经返回 `PostVO` 所需的主要字段，`PostServiceImpl` 还会执行 `enrichPostContent`。

因此搜索结果可以直接复用 `PostVO` 和 `PostCard`，不新增搜索结果 VO，不新增搜索专用后端返回结构。

## 10. 修改文件清单

### 10.1 新增文件

- `frontend/src/views/search/Search.vue`

### 10.2 预计修改文件

- `backend/src/main/java/sim/forum/dto/post/PostQueryDTO.java`
- `backend/src/main/java/sim/forum/service/impl/PostServiceImpl.java`
- `backend/src/main/resources/mapper/PostMapper.xml`
- `frontend/src/components/index/NavBar.vue`
- `frontend/src/router/index.ts`
- `frontend/src/models/post/postTypes.ts`

### 10.3 可能需要修改的文件

- `frontend/src/views/ForumIndex.vue`：仅当需要调整导航栏搜索值的传递方式或外壳行为时修改。
- `frontend/src/models/post/postStore.ts`：如果搜索页面选择复用 Store，可扩展查询状态；优先避免把搜索状态与首页/社区列表状态耦合。
- `frontend/src/api/post.ts`：接口封装已统一接收 `GetPostsDTO`，通常不需要修改；只有类型或调用约束需要调整时才改。

### 10.4 不应修改的范围

- 数据库表结构和 SQL 初始化脚本。
- 评论、用户、板块等无关模块。
- HOT 的无关评分算法。
- 独立搜索服务或 Redis 基础设施。

## 11. 设计取舍

1. 复用 `/posts` 而不是新增搜索接口：搜索只是帖子查询的可选过滤条件，避免接口重复。
2. 使用 MySQL LIKE：符合 V1 简单包含搜索目标，不引入 Elasticsearch、分词和额外运维成本。
3. 独立搜索页面：符合 Vue SPA 路由模型，搜索结果状态不污染首页列表状态。
4. 后端过滤而不是前端过滤：保证分页总数正确，避免只在当前已加载页中搜索。
5. HOT 保持独立查询：当前代码已经将 HOT 拆出，搜索只补充候选条件，降低对已有热度逻辑的影响。
6. 有效 keyword 绕过当前列表缓存：当前缓存 key 不包含 keyword 等查询条件，直接复用会返回错误结果；绕过是最小且安全的兼容处理。

## 12. 已知问题与风险

1. MySQL `LIKE '%keyword%'` 在帖子数量和正文长度增长后可能产生较高查询成本，V1 接受该限制；后续可根据真实数据评估进一步优化方案。
2. `posts.content` 当前为长文本，正文包含查询的成本可能高于标题查询，V1 暂不单独优化。
3. 当前帖子列表 Redis 缓存主要用于全站首页第一页。由于缓存 key 未包含 `pageNum`、`boardId`、`keyword` 等查询条件，开发时应限制缓存仅在符合现有首页缓存条件的请求中使用。搜索请求、指定 `boardId` 的请求以及非第一页请求不应复用该缓存。本 Feature 不全面重构现有 Redis 缓存机制。
4. 当前普通 `/posts` 查询没有显式过滤 `is_deleted`，前端存在过滤逻辑；本 Feature 不改变这一既有口径。
5. 当前 `HomeFeed.vue` 存在本地标题过滤逻辑，但该逻辑不是后端搜索。开发正式搜索时需确认其与新的搜索入口不存在冲突。
6. 数据库初始化脚本使用的排序规则支持大小写不敏感，但不同部署环境的排序规则可能存在差异，联调时需确认实际行为。

