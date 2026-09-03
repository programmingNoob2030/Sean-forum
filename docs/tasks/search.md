# 帖子搜索功能 V1 开发任务拆分

## 1. 执行说明

本任务清单供后续 Development Agent 使用。开发前必须阅读：

- `docs/requirements/search.md`
- `docs/design/search.md`
- `docs/project.md`
- `docs/architecture.md`

开发前先执行 `git status --short`，确认当前工作区已有用户改动，并且只在必要文件上工作。不要覆盖或回退已有修改。

本 Feature 只实现帖子标题/正文的简单包含搜索、搜索结果页和分页联动。禁止借此引入 Elasticsearch、分词、搜索高亮、搜索缓存、独立搜索接口或无关重构。

## 2. 任务依赖总览

```text
T1 请求参数与参数口径
  -> T2 普通帖子查询搜索条件
  -> T3 HOT 独立查询搜索条件
  -> T4 Service 分流、分页与缓存兼容
  -> T5 前端路由与搜索结果页
  -> T6 NavBar Enter 触发
  -> T7 搜索页分页与关键词变化
  -> T8 联调验收准备
```

T2 与 T3 在 T1 完成后可以并行；T4 依赖 T2、T3；T5/T6 可并行，但 T7 依赖 T5；T8 依赖全部前置任务。

## 3. T1：增加 keyword 请求参数

### 任务名称

扩展帖子列表查询参数

### 目的

让现有 `GET /posts` 能接收可选的 `keyword`，并明确 null、空字符串、空格和超长输入的处理口径。

### 涉及文件

- `backend/src/main/java/sim/forum/dto/post/PostQueryDTO.java`
- `frontend/src/models/post/postTypes.ts`

### 实现内容

1. 在 `PostQueryDTO` 增加可选字符串字段 `keyword`。
2. 前端 `GetPostsDTO` 增加可选 `keyword?: string`。
3. 不新增独立搜索 DTO。
4. 按设计文档统一处理：null、空字符串和 trim 后为空的关键词不产生搜索过滤条件。
5. 有效关键词首尾空格应被清理。
6. 按设计文档建议限制有效关键词长度为 `100` 个字符

### 前置依赖

- 无。

### 完成标准

- `/posts` 可以成功绑定 `keyword` query 参数。
- 不传 keyword 的旧请求仍可正常绑定。
- 前端请求类型允许搜索页传递 keyword。

## 4. T2：普通排序查询增加关键词过滤

### 任务名称

为 RECENT、POPULAR、COMMENTS 查询接入标题/正文搜索

### 目的

让普通帖子列表在有效关键词存在时，只返回标题或正文包含关键词的帖子，同时保持原有排序。

### 涉及文件

- `backend/src/main/resources/mapper/PostMapper.xml`

### 实现内容

1. 在普通帖子查询的候选过滤区域加入可选 keyword 条件。
2. 条件逻辑必须是：
   - `title LIKE keyword`
   - 或 `content LIKE keyword`
3. keyword 条件与现有 `boardId` 条件使用 AND 组合。
4. 对 OR 条件加括号，避免改变 boardId 过滤范围。
5. 使用 MyBatis 参数绑定，不把用户输入直接拼进 SQL 文本。
6. 处理 LIKE 特殊字符，使 `%`、`_` 等输入按普通字符处理，并保持查询安全。
7. keyword 为空时不生成搜索条件。
8. 保持排序语义：RECENT 按发布时间、POPULAR 按现有点赞字段、COMMENTS 按现有评论字段。

### 前置依赖

- T1。

### 完成标准

- 标题命中、正文命中、标题和正文同时命中均符合需求。
- 同一帖子不会因为两个字段都命中而重复返回。
- 无 keyword 时 SQL 查询行为与修改前一致。
- 现有 RECENT、POPULAR、COMMENTS 排序不改变。

## 5. T3：HOT 独立查询接入关键词过滤

### 任务名称

为 HOT 查询增加候选帖子搜索条件

### 目的

让 HOT 搜索只在关键词匹配的帖子集合中计算和排序热门度，且不破坏现有 HOT 算法。

### 涉及文件

- `backend/src/main/resources/mapper/PostMapper.xml`
- 如确有必要：`backend/src/main/java/sim/forum/mapper/PostMapper.java`

### 实现内容

1. 保留现有独立 `getHotPosts` 查询，不重构 HOT 查询层级。
2. 将 keyword 过滤放在 HOT 最内层候选帖子集合的位置。
3. keyword 与 boardId 共同限制候选集合。
4. 确保 keyword 在 HOT 计算前参与帖子候选集合过滤，不在 HOT 结果生成后再过滤。
5. 不在 HOT 最外层排序后再过滤 keyword。
6. keyword 为空时保持 HOT 原有查询行为。
7. 保持现有 HOT 排序和分页前全局排序语义。

### 前置依赖

- T1。

### 完成标准

- `keyword + sort=HOT` 只返回匹配帖子。
- HOT 的热门度顺序在匹配候选集合内正确计算。
- 无 keyword 的 HOT 结果和排序逻辑不被改变。
- 不复制出第二套不一致的 HOT 算法。

### 6. T4：Service 分流、分页和现有缓存兼容

### 任务名称

接入 keyword 查询并保护搜索分页结果

### 目的

让普通排序和 HOT 排序正确选择 Mapper 查询，并明确现有首页 Redis 缓存的使用边界，避免不同查询条件错误复用首页缓存。

### 涉及文件

- `backend/src/main/java/sim/forum/service/impl/PostServiceImpl.java`
- 必要时：`backend/src/main/java/sim/forum/service/PostService.java`

### 实现内容

1. 保留当前 `HOT -> getHotPosts`、其他排序 -> `getPosts` 的分流。
2. 保留当前 `PageHelper.startPage(num, size)` 紧贴数据库查询的要求。
3. 仅当 `pageNum = 1`、`boardId` 为空且无有效 `keyword` 时使用现有 Redis 列表缓存。
4. 其他请求直接执行数据库查询，不读取或写入现有 Redis 列表缓存，包括非第一页、指定 `boardId` 和带有效 `keyword` 的请求。
5. 不修改现有 Redis key 结构，不新增 Redis 搜索缓存。
6. 保持 `enrichPostContent` 和 `PageResult.of(new PageInfo<>(list))` 流程。
7. 保持 `/posts` Controller、返回结构和可选认证行为不变。
8. 如果需要调整 Service 接口，只做与查询参数或现有分流直接相关的最小修改。

### 前置依赖

- T2、T3。

### 完成标准

- `keyword` 与四种 sort 均可组合请求。
- 搜索分页 total 是匹配结果总数。
- 带 keyword 的第一页和后续页不会互相串数据。
- 非首页查询不会错误读取首页 Redis 缓存。
- 现有首页缓存行为不因搜索功能发生无关变化。

## 7. T5：新增搜索结果页面和 Router 路由

### 任务名称

建立 `/search?keyword=xxx` 独立搜索页面

### 目的

使用现有 Vue Router 和帖子展示能力承载搜索结果。

### 涉及文件

- 新增：`frontend/src/views/search/Search.vue`
- `frontend/src/router/index.ts`
- 可复用：`frontend/src/api/post.ts`
- 可复用：`frontend/src/components/post/PostCard.vue`

### 实现内容

1. 新增独立搜索结果页面，不使用 `window.open()`。
2. 注册 `/search` 路由，支持 query 参数 `keyword`。
3. 页面从 `route.query.keyword` 获取关键词，并在刷新后恢复。
4. 使用已有 `apiGetPosts` 请求 `/posts`，请求参数包含 keyword、pageNum、pageSize 和 sort。
5. 复用 `PostVO` 和 `PostCard` 展示结果。
6. 展示匹配结果总数和空结果状态。
7. 默认排序使用 `RECENT`，并保留 `POPULAR`、`COMMENTS`、`HOT`。
8. 点击帖子后沿用现有帖子详情路由行为。

### 前置依赖

- T1。

### 完成标准

- 直接访问 `/search?keyword=redis` 可以加载搜索结果。
- 页面能显示列表和总数量。
- 搜索页面不会改变首页列表状态。
- 页面不创建新窗口。

## 8. T6：NavBar 增加 Enter 搜索触发

### 任务名称

连接导航栏搜索框与搜索路由

### 目的

让用户从现有导航栏搜索框按 Enter 进入搜索结果页面。

### 涉及文件

- `frontend/src/components/index/NavBar.vue`
- 必要时：`frontend/src/views/ForumIndex.vue`

### 实现内容

1. 保留现有 `el-input` UI 和双向绑定。
2. 增加 Enter 事件处理。
3. 对输入值执行首尾空白清理。
4. 有效关键词通过 Vue Router 导航到 `/search?keyword=xxx`。
5. 使用 Router query 传参，保证关键词正确编码。
6. 空或纯空格关键词按无条件帖子列表处理，并进入 `/search` 页面；请求 `/posts` 时不生成 keyword 搜索过滤条件。
7. 不使用 `window.open()`。

### 前置依赖

- T5。

### 完成标准

- 在任意包含 NavBar 的页面输入关键词并按 Enter，能进入正确 search 路由。
- 中文、空格和 URL 特殊字符不会破坏路由参数。
- 清空关键词不会保留旧搜索结果触发条件。

## 9. T7：搜索页面分页、排序和关键词变化

### 任务名称

完成搜索结果页状态联动

### 目的

保证搜索结果的分页、排序和关键词切换符合需求。

### 涉及文件

- `frontend/src/views/search/Search.vue`
- 必要时：`frontend/src/models/post/postTypes.ts`
- 必要时：`frontend/src/api/post.ts`

### 实现内容

1. 使用 Element Plus `el-pagination`。
2. 分页控件使用接口返回的 total、pageNum 和 pageSize。
3. 翻页时保持当前 keyword，并重新调用 `/posts`。
4. 搜索页切换 sort 时保持 keyword，并将 pageNum 重置为 1。
5. 路由 query keyword 变化时，将 pageNum 重置为 1 并重新请求。
6. 处理无结果、请求中和请求失败状态，沿用项目现有页面风格。
7. 避免用前端本地过滤替代后端搜索。

### 前置依赖

- T5、T6。

### 完成标准

- 翻页请求 URL 仍带 keyword。
- 修改关键词后不会停留在旧页码。
- sort=HOT 搜索时仍能正常分页。
- total 与当前关键词匹配数量一致。

## 10. T8：集成验证与开发自检

### 任务名称

完成搜索功能基础自检并生成测试说明

### 目的

确认本 Feature 的主要链路已经完成，进行必要的开发自检，并为 Testing Agent 提供明确、可复现的测试场景。

本任务不负责完整测试，不扩大功能范围。

### 涉及文件 / 模块

- `/posts` 查询链路
- `NavBar.vue`
- 搜索结果页面
- Router
- `PostMapper.xml`
- `tests.md`

### 实现内容

1. 对搜索功能进行基础自检，至少确认：
   - NavBar 输入关键词后可以进入 `/search`。
   - 搜索结果页面可以正常请求 `/posts`。
   - `keyword` 可以正确传递到后端。
   - 标题或正文命中时可以返回结果。
   - `RECENT`、`POPULAR`、`COMMENTS`、`HOT` 四种排序均能正常配合关键词搜索。
   - 搜索分页、总数和关键词变化的基本行为正常。
   - 无关键词时原有 `/posts` 行为未被破坏。
   - 搜索请求不会错误复用现有 Redis 列表缓存。
2. 不在本任务中进行完整边界测试、性能测试或深入缺陷排查。
3. 在项目中生成 `docs/tests.md`，记录本 Feature 需要由 Testing Agent 执行的正式测试场景。
4. `tests.md` 应覆盖至少以下测试方向：
   - 标题命中。
   - 正文命中。
   - 标题或正文命中。
   - 标题、正文同时命中时不重复返回。
   - 大小写差异。
   - 中文关键词及包含空格的关键词。
   - `%`、`_` 等 LIKE 特殊字符。
   - null、空字符串、纯空格关键词。
   - 超长关键词。
   - keyword 与 RECENT、POPULAR、COMMENTS、HOT 组合。
   - 搜索结果分页及总数。
   - 修改关键词后回到第一页。
   - 无结果状态。
   - 未登录访问。
   - Redis 缓存边界条件。
   - SQL 关键字作为搜索关键词时能够正常搜索，不应因为关键词本身具有 SQL 语义而产生异常或改变查询逻辑。
5. `tests.md` 只描述测试目标、前置条件、操作步骤、预期结果等必要信息，不在本任务中提前修改测试结果或虚构测试结论。

### 依赖

T1、T2、T3、T4、T5、T6、T7

### 验收标准

- 搜索主要链路能够完成一次基本自检。
- 未发现明显的功能阻断问题。
- 项目中已生成 `tests.md`。
- `tests.md` 能够作为 Testing Agent 后续正式测试的输入。
- 未因本任务扩大搜索 V1 的功能范围。

## 11. 不得扩大范围

开发过程中不得将以下事项加入任务：

- 新增 `/search` 后端接口。
- Elasticsearch 或分词。
- 搜索相关性评分、高亮、建议或历史。
- Redis 搜索缓存设计。
- 修改数据库表结构或新增搜索字段。
- 重构 HOT 算法。
- 统一修复所有帖子逻辑删除返回口径。
- 重构首页、导航栏或帖子列表无关样式。

