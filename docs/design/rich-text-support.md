# Rich Text Support 技术设计

## 1. 设计概述

### 1.1 目标

在不改变现有单体架构和帖子、评论业务闭环的前提下，实现：

- 帖子正文支持普通文字、换行、emoji 和正文内嵌图片。
- 图片按照用户插入顺序显示在正文内容流中，不建立独立附件列表。
- 评论继续使用文本内容，但允许直接输入和保存 emoji。
- 历史纯文本帖子和评论继续可读。
- 不把用户输入直接当作 HTML 执行，避免 XSS 和页面结构破坏。

### 1.2 方案选择

帖子正文采用**受限内容节点 JSON**作为新内容的规范存储格式，而不是直接存储用户提交的 HTML：

```json
{
  "version": 1,
  "nodes": [
    { "type": "text", "text": "第一段文字\n" },
    { "type": "image", "path": "2026/08/24/post/content/12/8f2....png" },
    { "type": "text", "text": "图片后的文字 😀" }
  ]
}
```

只允许 `text` 和 `image` 两种节点。文字节点中的换行保留段落结构，emoji 作为文字节点中的 Unicode 字符保存。图片节点只保存服务端生成的相对路径，不接受外部 URL、`data:` URL 或任意 HTML。

选择该方案的原因：

1. 可以明确表达文字、图片的顺序，不需要依赖 HTML 解析。
2. 展示时可以使用 Vue 模板渲染，避免将用户输入交给 `v-html`。
3. 继续复用现有 `posts.content` 的 `MEDIUMTEXT` 字段，不新增附件表。
4. 不需要为基础富文本引入编辑器、消息队列或对象存储。
5. 可以通过 `version` 为后续内容格式扩展保留兼容空间。

评论不采用节点 JSON。评论本期不支持图片，现有 `comments.content` 文本字段已经能够保存换行和 emoji，继续沿用即可。

## 2. 影响模块分析

### 2.1 后端模块

| 模块 | 影响 | 改动内容 |
| --- | --- | --- |
| `PostController` | 直接影响 | 增加帖子正文图片上传接口；保留现有发帖、查询接口 |
| `PostService` / `PostServiceImpl` | 直接影响 | 校验、规范化帖子节点文档；校验图片路径；组装展示节点和摘要 |
| `FileUploadService` | 直接影响 | 增加帖子正文图片的格式、大小、路径和目录规则 |
| `CreatePostDTO` | 直接影响 | `content` 不再依赖原始 JSON 字符串长度注解，正文长度改由内容解析器按文字节点校验 |
| `Post` | 轻度影响 | Java 字段仍为 `String`，数据库字段不变；新增内容格式说明或辅助转换不应放入 Entity |
| `PostVO` / `RecentPostVO` | 直接影响 | 增加解析后的节点、文本摘要和首图路径，供不同展示场景使用 |
| `PostMapper` / `PostMapper.xml` | 轻度影响 | 现有正文查询保持；如通过 SQL 直接查询新增展示字段，仅做必要调整 |
| `GlobalExceptionHandler` | 间接影响 | 复用 `BusinessException` 返回明确的内容校验和图片上传错误 |
| `WebConfig` | 检查影响 | 继续复用 `/uploads/**` 静态资源映射；不新增开放的任意文件访问路径 |
| `CommentController` / `CommentServiceImpl` | 轻度影响 | 保持接口和事件链路，补充空白评论校验时不得误判仅 emoji 评论 |
| 后端测试 | 直接影响 | 增加节点解析、长度计算、图片路径和历史纯文本兼容测试 |

### 2.2 前端模块

| 模块 | 影响 | 改动内容 |
| --- | --- | --- |
| `PostEdit.vue` | 直接影响 | 普通 textarea 替换为基础内容编辑器，支持图片选择、拖拽和 emoji 文本输入 |
| 新增 `PostContentEditor.vue` | 直接影响 | 管理编辑光标、图片插入、上传状态、内容节点序列化 |
| 新增 `PostContentRenderer.vue` | 直接影响 | 安全渲染帖子正文节点和图片加载失败占位 |
| 新增或复用摘要组件 | 直接影响 | 帖子列表仅显示文字摘要和首张图片缩略图 |
| `PostDetail.vue` | 直接影响 | 使用正文渲染器；评论输入继续支持 emoji；提交失败保留内容 |
| `PostCard.vue` / `RecentPostCard.vue` | 直接影响 | 使用摘要和首图字段，不直接渲染原始 `content` |
| `CommentCard.vue` / `SubCommentItem.vue` | 轻度影响 | 继续使用文本插值和 `white-space: pre-wrap` 展示评论；输入框允许 emoji |
| `api/post.ts` | 直接影响 | 增加帖子正文图片上传 API；扩展帖子响应类型 |
| `postTypes.ts` | 直接影响 | 增加节点、正文展示元数据和图片上传响应类型 |
| `commentTypes.ts` | 轻度影响 | 保持 `content: string`，如补充长度类型约束则同步前端校验 |
| `utils/requests.ts` / 环境配置 | 检查影响 | 确保 multipart 上传和静态资源 URL 继续使用现有请求配置 |

### 2.3 不受影响的模块

以下模块不需要因为内容形式变化而改变业务语义：

- 点赞 / 点踩及其计数更新。
- 评论计数、评论事件和站内消息发送。
- 帖子逻辑删除、恢复和浏览记录。
- 社区成员权限、帖子创建权限和 JWT 登录拦截。
- 数据库表之间的关系。

## 3. 后端接口设计

### 3.1 上传帖子正文图片

#### 请求

```http
POST /posts/images
Content-Type: multipart/form-data
Authorization: Bearer <token>
```

表单字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `file` | `MultipartFile` | 是 | 单张 `jpg`、`jpeg` 或 `png` 图片 |

鉴权要求：必须登录，不使用 `@OptionalAuth`。

#### 成功响应

继续使用统一响应体。为保持当前上传接口风格，可以返回相对路径字符串：

```json
{
  "code": 200,
  "message": "success",
  "data": "2026/08/24/post/content/12/8f2....png"
}
```

前端通过现有 `VITE_RESOURCE_URL + path` 生成展示地址。相对路径由服务端生成，前端不得自行拼接物理路径或文件名。

#### 处理规则

1. 校验文件非空。
2. 校验单张文件大小不超过 `10MB`。
3. 校验扩展名只允许 `jpg`、`jpeg`、`png`。
4. 同时读取文件内容判断实际图片类型，不能只依赖文件名后缀或客户端 `Content-Type`。
5. 使用现有本地文件服务和 UUID 文件名保存。
6. 建议目录格式为：
   `yyyy/MM/dd/post/content/{userId}/`
7. 使用规范化路径确认目标文件位于 `file.upload-path` 下，防止路径穿越。
8. 成功后返回相对路径；失败时不返回可写入正文的路径。

本期不设置业务层图片数量和帖子总图片大小上限，但每次上传仍受单文件限制、HTTP 请求限制、磁盘容量和服务器运行策略约束。后续若资源占用成为问题，应增加配置化配额，而不是修改正文格式。

### 3.2 创建帖子

#### 请求

现有接口保持不变：

```http
POST /posts
Content-Type: application/json
Authorization: Bearer <token>
```

请求字段仍为：

```json
{
  "boardId": 1,
  "title": "帖子标题",
  "content": "{\"version\":1,\"nodes\":[...]}"
}
```

`content` 仍是字符串字段，以避免改变现有请求 DTO 的外部形状。新前端提交内容节点 JSON；旧客户端提交的非 JSON 普通文本也应继续接受并按历史纯文本处理。

#### 服务端校验

1. 保留标题非空、最大长度 100 和社区选择校验。
2. 新节点文档必须是合法 JSON，版本必须受支持。
3. 节点类型只能是 `text` 或 `image`。
4. `text` 节点只能包含文本和换行，不能解释其中的 HTML。
5. `image.path` 必须是服务端生成的帖子正文图片相对路径，并且路径归属于当前用户的帖子图片目录。
6. 图片文件必须存在且仍是允许的图片类型。
7. 帖子正文的长度只统计所有 `text` 节点，不统计图片路径、节点 JSON 结构和图片数量。
8. emoji 作为文本参与长度计算。建议按 Unicode code point 计数，避免一个常见 emoji 的 UTF-16 代理对被计为两个字符；不额外实现复杂的用户可见 grapheme 聚类统计。
9. 允许仅包含图片、仅包含 emoji 或二者组合；但不能是没有图片且文字节点去除空白后为空的内容。
10. 校验失败时不得插入 `posts` 记录。

非 JSON 的历史兼容输入按纯文本校验和保存，不作为 HTML 执行。格式不合法且看起来是新节点文档的内容应直接拒绝，不能静默降级为 HTML。

#### 成功响应

现有 `Result<Post>` 响应可以暂时保持，以减少调用方影响。后续若需要统一展示字段，可将创建接口响应改为 `PostVO`，但不作为本期必须项，因为前端发布成功后会刷新列表和详情。

### 3.3 查询帖子

以下接口路径和鉴权保持不变：

- `GET /posts`
- `GET /post/{id}`
- `GET /posts/history`

响应在现有字段基础上增加展示字段：

```json
{
  "id": 100,
  "title": "图文帖子",
  "content": "{\"version\":1,\"nodes\":[...]}",
  "contentFormat": "BLOCKS",
  "contentNodes": [
    { "type": "text", "text": "正文 😀" },
    { "type": "image", "path": "2026/08/24/post/content/12/8f2....png" }
  ],
  "contentTextPreview": "正文 😀",
  "firstImagePath": "2026/08/24/post/content/12/8f2....png"
}
```

字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `content` | `string` | 数据库存储的原始内容，保留兼容和调试需要；前端展示不直接使用 |
| `contentFormat` | `string` | `PLAIN` 或 `BLOCKS` |
| `contentNodes` | `array` | 详情页按顺序渲染的规范化节点；历史文本转换为一个或多个 text 节点 |
| `contentTextPreview` | `string` | 列表摘要使用的纯文本内容，不包含图片路径 |
| `firstImagePath` | `string/null` | 列表首图缩略图路径 |

`PostVO` 使用详情字段；`RecentPostVO` 至少增加 `contentTextPreview` 和 `firstImagePath`。如果列表需要完整内容流，也可以复用 `contentNodes`，但不建议让列表渲染完整长正文。

### 3.4 创建评论

现有接口保持不变：

```http
POST /comments
Content-Type: application/json
Authorization: Bearer <token>
```

`content` 仍为普通文本字符串，可以包含 emoji 和换行。服务端校验：

- 去除空白后为空时拒绝。
- 仅包含 emoji 时允许。
- 图片节点、HTML 或其他结构化内容不作为评论能力接受；若客户端传入类似内容，应按普通文本显示，或在明确禁止 HTML 的实现中拒绝，不得执行。

评论成功后继续触发：

- 帖子评论数更新事件。
- 评论通知消息。
- 现有评论分页、删除、恢复和点赞流程。

## 4. 数据结构变化

### 4.1 数据库

本期不新增表、不新增字段、不修改外键和索引。

原因：

- `posts.content` 已经是 `MEDIUMTEXT`，足以保存节点 JSON。
- 图片是正文节点引用的本地资源，不建立附件记录。
- `comments.content` 的 `TEXT` 类型足以保存文字和 emoji。
- 删除、恢复、点赞、评论计数等业务均围绕帖子/评论 ID 工作，不依赖正文格式。

需要在数据库文档和迁移说明中补充：新帖子 `posts.content` 可能保存版本化 JSON，历史记录仍可能是纯文本。

### 4.2 后端内容模型

建议新增帖子内容专用模型，放在 `dto.post.content` 或 `vo.post.content` 下，不复用数据库 Entity：

```text
PostContentDocument
  version: Integer
  nodes: List<PostContentNode>

PostContentNode
  type: TEXT | IMAGE
  text: String       // TEXT 节点使用
  path: String       // IMAGE 节点使用

PostContentNodeVO
  type: TEXT | IMAGE
  text: String
  path: String
```

字段互斥规则：

- `TEXT` 只能有 `text`。
- `IMAGE` 只能有 `path`。
- 不接受任意额外 HTML、样式、事件属性、外部 URL、尺寸属性或下载属性。

建议新增 `PostContentCodec` 或同职责的帖子内容服务，集中处理：

- JSON 解析与版本判断。
- 节点白名单校验。
- 文本长度和有效内容判断。
- 历史纯文本转换。
- 摘要和首图提取。

这样 Controller、Mapper 和前端展示模块不需要各自重复解析规则。

### 4.3 前端类型

建议在 `frontend/src/models/post/postTypes.ts` 中增加：

```text
PostContentNode
  type: 'text' | 'image'
  text?: string
  path?: string

PostContentView
  format: 'PLAIN' | 'BLOCKS'
  nodes: PostContentNode[]
  textPreview: string
  firstImagePath?: string

PostImageUploadResponse
  path: string
```

`CreatePostDTO` 的字段形状不变，`content` 仍为字符串。`CommentDTO` 不改变字段形状。

## 5. 前端改动范围

### 5.1 帖子编辑器

新增 `PostContentEditor.vue`，替换 `PostEdit.vue` 中的 textarea。组件职责：

1. 使用原生 `contenteditable` 作为轻量编辑区域，不引入第三方富文本编辑器。
2. 普通文字、换行和 emoji 均作为文本节点输入。
3. 提供选择文件按钮，仅允许选择 `jpg/jpeg/png`。
4. 支持将本地图片文件拖拽到编辑区域。
5. 图片上传成功后在当前光标位置插入不可直接编辑的图片节点。
6. 上传失败时不插入图片节点，并提示“该图片未能上传，未加入正文”；用户可以继续编辑和发布。
7. 插入图片后保留图片前后的输入位置，用户可以继续输入文字或 emoji。
8. 提交前将编辑区域 DOM 序列化为 `PostContentDocument` JSON。
9. 维护 `pendingUpload` 状态，存在正在上传或失败未处理的节点时禁止提交不完整文档。
10. 发布请求失败时不清空标题和正文，允许用户继续修改或重试；只有发布成功后才重置表单。

图片节点必须使用 `contenteditable="false"` 和固定的最小布局约束，避免编辑时图片被光标拆分或撑破编辑区。删除图片只从正文节点中移除，不需要额外的附件管理界面。

emoji 不建立单独节点，也不提供固定表情资源面板。用户使用操作系统或浏览器自带的 emoji 输入方式直接输入，保存和展示都按普通 Unicode 文本处理。

### 5.2 帖子详情渲染

新增 `PostContentRenderer.vue`：

- 遍历 `contentNodes`。
- 文本节点用普通 Vue 文本插值输出，并使用 CSS 的 `white-space: pre-wrap` 保留换行。
- 图片节点使用 `<img>`，设置 `max-width: 100%`、`height: auto`、`display: block`，避免横向溢出。
- 使用 `loading="lazy"` 降低详情页首次加载压力。
- 图片加载失败时只替换当前图片为占位提示，正文其他文字、emoji 和图片继续显示。
- 不使用 `v-html`。

历史 `PLAIN` 内容由服务端转换为 text 节点，或者由渲染器将 `content` 当作纯文本处理，不能因为历史内容中存在 `<`、`>` 而执行标签。

### 5.3 帖子列表和最近浏览

`PostCard.vue`、`RecentPostCard.vue` 不渲染完整正文节点：

- 优先展示 `contentTextPreview`。
- 如果存在 `firstImagePath`，在摘要区域显示首图缩略图。
- 首图加载失败时显示固定占位，不影响标题、摘要和操作区。
- 摘要使用固定最大高度和 `overflow: hidden`，避免长正文或大图片改变列表卡片结构。

### 5.4 评论输入和展示

`PostDetail.vue`、`CommentCard.vue`、`SubCommentItem.vue` 继续使用 textarea 和普通文本插值：

- 放开或保留浏览器输入 emoji 的能力。
- 使用 `white-space: pre-wrap` 保留换行。
- 禁止增加图片按钮和图片上传逻辑。
- 发送按钮的空值判断使用 `trim()`，但不能通过“是否包含普通字母/汉字”判断有效性，以允许仅 emoji 评论。
- 请求失败时保留当前输入，成功后清空并刷新评论和帖子计数。

## 6. 核心业务流程

### 6.1 发布带图片的帖子

```text
用户打开 PostEdit
  -> 输入标题和文字/emoji
  -> 选择文件或拖拽本地图片
  -> 前端调用 POST /posts/images
  -> 后端校验文件并保存到本地上传目录
  -> 返回相对路径
  -> 前端在当前光标位置插入 image 节点
  -> 用户继续编辑
  -> 前端序列化 nodes 为 content JSON
  -> 调用现有 POST /posts
  -> PostService 解析并校验内容文档
  -> 插入 posts 记录
  -> 发布 PostCreateEvent
  -> 返回成功，前端刷新列表或详情
```

图片上传失败时，在“返回相对路径”之前流程结束，编辑器不插入图片节点，帖子仍可继续发布已有内容。

### 6.2 发布纯文本或 emoji 帖子

新前端仍发送节点 JSON，其中所有内容都在 `text` 节点中。服务端按文字节点计算长度和有效性，emoji 与普通文本使用同一校验路径。

旧客户端发送普通文本时，服务端接受非 JSON 的非空内容作为历史兼容格式，按纯文本保存和展示。

### 6.3 发布评论

```text
用户在评论输入框输入文字/emoji
  -> 前端 trim 判断空白
  -> 调用现有 POST /comments
  -> CommentService 校验内容并写入 comments
  -> 发布 CommentCreateEvent
  -> 更新帖子评论数
  -> 异步发送站内消息
  -> 返回评论并刷新评论列表
```

正文格式不会改变评论事件的 target、rootId、parentId 和消息接收者计算。

### 6.4 浏览帖子

```text
PostMapper 查询 posts.content
  -> PostService 使用 PostContentCodec 解析
  -> 新文档输出 BLOCKS 节点
  -> 历史纯文本输出 PLAIN/text 节点
  -> 同时提取 textPreview 和 firstImagePath
  -> 前端按场景使用 Renderer 或 Preview
```

解析失败的已存在内容不能导致整个帖子列表或详情接口失败。建议记录告警并返回安全的纯文本占位结果；对于无法解析的 JSON，不得把原始字符串交给 `v-html`。

## 7. 异常处理方案

### 7.1 图片上传异常

| 场景 | 服务端行为 | 前端行为 |
| --- | --- | --- |
| 文件为空 | 返回业务错误 | 不插入正文，提示上传失败 |
| 扩展名不支持 | 返回业务错误 | 提示只支持 jpg/jpeg/png |
| 实际文件类型不匹配 | 返回业务错误 | 提示图片格式无效 |
| 单张超过 10MB | 返回业务错误 | 保留其他正文内容，提示文件过大 |
| 目录创建或磁盘写入失败 | 记录必要日志并返回业务错误 | 不插入图片，可继续编辑 |
| 文件上传请求超时 | 返回请求失败 | 保留编辑器已有内容 |

不向用户返回服务器物理路径、堆栈信息或敏感配置。

### 7.2 帖子正文异常

建议使用现有 `BusinessException` 和全局异常处理，至少覆盖：

- 内容格式不受支持。
- 节点类型不受支持。
- 正文文字超过 20000 个字符。
- 正文不包含有效文字、emoji 或图片。
- 图片路径不属于当前用户的帖子图片目录。
- 图片文件不存在或已损坏。
- 内容包含外部 URL、脚本、HTML 属性或不允许字段。

校验必须在数据库插入前完成，避免产生缺图或残缺帖子。

### 7.3 评论异常

- 仅空白字符的评论返回“评论内容不能为空”。
- 仅 emoji 的评论视为有效内容。
- 评论插入失败时不发布评论创建事件，不更新计数，也不发送通知。
- 保持现有删除、恢复权限校验。

### 7.4 图片展示异常

图片不能加载时：

- 仅当前图片显示“图片加载失败”占位。
- 保留图片前后的文字、emoji 和其他图片。
- 不将失效图片错误地转换成下载链接或独立附件。

## 8. 与已有功能的兼容性分析

### 8.1 数据兼容

- 不修改 `posts` 和 `comments` 表结构。
- 历史纯文本帖子不迁移、不重写。
- 新帖子内容使用 JSON，`MEDIUMTEXT` 容量保持不变。
- 评论仍保存纯文本，历史评论无需迁移。

### 8.2 接口兼容

- `POST /posts` 的字段名和 URL 不变，`content` 仍为字符串。
- `POST /comments` 的请求字段和 URL 不变。
- 帖子查询接口 URL、分页参数和既有业务字段不变，仅增加展示字段。
- 新增 `/posts/images` 不影响头像和社区封面上传接口。
- 旧客户端提交纯文本帖子时，服务端保留兼容处理。

### 8.3 业务兼容

- 发帖仍由登录拦截器和现有社区权限规则保护。
- 帖子创建成功后仍发布 `PostCreateEvent`，社区和用户统计行为不变。
- 帖子删除、恢复只操作帖子状态，不解析或重写正文。
- 帖子点赞/点踩只依赖帖子 ID 和计数，不受正文节点影响。
- 评论创建仍触发评论计数和站内消息；emoji 不影响接收者计算。
- 浏览记录仍只保存帖子 ID，不保存正文内容。

### 8.4 展示兼容

- 新前端不再直接使用 `v-html` 渲染帖子正文。
- 历史帖子以纯文本节点显示，换行继续可读。
- 帖子列表使用摘要和首图，不把完整 JSON 展示给用户。
- 评论继续使用文本插值，不执行用户输入。

### 8.5 文件生命周期和资源风险

图片在正式发布帖子前独立上传，因此用户取消发帖、删除图片或发布失败时可能留下未被正文引用的本地文件。本期不引入附件表、草稿表或独立资源管理系统，建议：

- 上传目录按日期和用户分目录，便于排查。
- 正文提交时只接受当前用户目录下、实际存在且格式有效的文件。
- 后续根据磁盘使用情况增加定期清理未被引用文件的运维任务；该任务不属于本期功能必需项。

## 9. 未决问题与本设计采用的口径

需求文档已经给出主要确认结果，但仍存在以下实现边界：

1. **图片上传失败与缺图发布的表述冲突**  
   本设计采用：上传失败的图片不进入正文，用户可以继续发布；已经进入正文但路径无效、文件不存在或上传尚未完成时，禁止提交该不完整文档。

2. **总图片数量和总大小“不限制”与服务器资源限制的关系**  
    本期不限制图片数量也不设置业务层总大小限制，但开发实现必须保留配置化限制能力和
保留后续增加限制的扩展点。但保留单张 10MB、单次请求限制、磁盘容量和运行环境保护。服务器保护不是产品层面的图片数量规则。

3. **评论长度规则在当前代码中的落点需要核对**  
   当前需求要求 emoji 按整体评论长度规则校验，但现有 `CommentDTO` 未体现完整的 Jakarta Validation 注解。开发阶段应先确认既有评论长度上限；若不存在，应在开发任务中补充明确上限，不能让帖子和评论使用两套未定义规则。

4. **emoji 的“一个表情占几个字符”**  
   本设计按 Unicode code point 计数，不做复杂的用户可见 grapheme 聚类。若产品必须按视觉表情数量或用户可见字符计数，需要另行确认并调整校验器。

## 10. 开发验证建议

开发实现完成后至少验证：

- 节点 JSON 的合法性、版本兼容和非法字段拒绝。
- 文字、换行、emoji、图片的多种组合和顺序。
- 仅图片、仅 emoji、空白正文的边界。
- 图片格式、实际 MIME、10MB 限制、路径穿越和外部 URL 拒绝。
- 图片上传失败不插入节点，发帖提交失败保留编辑内容。
- 详情、列表、最近浏览的摘要和首图展示。
- 失效图片不影响其他正文内容。
- 历史纯文本帖子和评论展示。
- 评论仅 emoji、评论空白、评论通知和评论计数。
- 删除、恢复、点赞、点踩和浏览记录回归。

