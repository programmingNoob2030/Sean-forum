# 项目架构说明

## 1. 整体架构

项目采用前后端分离的单体架构：

```text
Vue 3 + Element Plus
        |
        | Axios / JSON / Multipart
        v
Spring MVC Controller
        |
        v
Service / ServiceImpl
        |
        +--> MyBatis-Plus Mapper
        |          |
        |          v
        |        MySQL
        |
        +--> StringRedisTemplate
        |          |
        |          v
        |        Redis
        |
        +--> Spring Event / Async Thread Pool
        |
        +--> Local File System
```

### 请求处理流程

1. 前端通过 Axios 访问 `/api` 前缀接口。
2. Vite 开发服务器将 `/api` 请求代理到后端 `http://127.0.0.1:8088`，并去掉 `/api` 前缀。
3. 后端 `LoginInterceptor` 处理 JWT，并将用户 ID 放入 `UserContext` 的 ThreadLocal。
4. Controller 接收请求参数，使用 DTO 和 `@Valid` 做部分参数校验。
5. Service 负责业务判断、事务、事件发布、缓存和文件上传协调。
6. Mapper / MyBatis XML 执行数据库查询或更新。
7. Controller 使用 `Result` 或 `PageResult` 返回统一结构。
8. 请求结束后，拦截器清理 ThreadLocal 中的用户 ID。

## 2. 项目目录结构

### 后端

`backend/src/main/java/sim/forum`：

- `annotation`：自定义注解，例如 `@OptionalAuth`。
- `config`：Spring MVC、异步线程池等配置。
- `context`：请求级用户上下文，当前使用 ThreadLocal 保存用户 ID。
- `controller`：REST API 入口，只负责接收请求、调用 Service 和封装返回值。
- `dto`：请求参数对象，按业务模块划分。
- `entity`：MyBatis-Plus 对应的数据库实体。
- `event`：Spring Application Event 的事件对象。
- `exception`：业务异常和全局异常处理。
- `interceptor`：JWT 登录拦截和可选认证处理。
- `mapper`：MyBatis Mapper 接口，继承 MyBatis-Plus `BaseMapper` 或声明自定义 SQL 方法。
- `result`：统一响应体和分页响应体。
- `service`：业务服务接口、通用服务和文件上传服务。
- `service/impl`：业务服务实现。
- `utils`：JWT 等通用工具。
- `vo`：面向前端的视图对象，通常包含联表查询或脱敏后的展示数据。
- `ForumApplication.java`：Spring Boot 启动类。

`backend/src/main/resources`：

- `mapper`：MyBatis XML 映射文件。
- `application.yaml.example`：后端配置示例，不包含真实连接凭据。

### 前端

`frontend/src`：

- `api`：Axios 接口封装。
- `assets`：前端构建资源。
- `components`：可复用业务组件，按 board、comment、index、message、post 等模块划分。
- `models`：TypeScript 类型定义和 Pinia Store。
- `router`：Vue Router 路由配置。
- `utils`：请求封装、认证辅助、时间格式化等工具。
- `views`：页面级组件和路由入口。
- `App.vue`：Vue 根组件。
- `main.ts`：应用启动入口，注册 Router、Pinia 和 Element Plus。

### SQL 与文档

- `sql/forum_db.sql`：数据库初始化脚本。
- `sql/alter_posts_content_to_mediumtext.sql`：已有数据库的帖子正文字段迁移脚本。
- `docs/images`：项目展示截图和 GIF。
- `docs/project.md`：项目状态知识库。
- `docs/architecture.md`：架构和编码约束知识库。

## 3. 核心业务模块设计

### 用户模块

主要类：

- `UserController`
- `UserService` / `UserServiceImpl`
- `UserMapper`
- `User`、用户 DTO、`LoginVO`

主要职责：注册、登录、JWT 生成、邮箱验证码、密码重置、个人资料更新和头像上传。

关键调用关系：

```text
UserController
  -> UserServiceImpl
       -> UserMapper
       -> StringRedisTemplate（验证码）
       -> MailService（邮件验证码）
       -> FileUploadService（头像）
       -> JWTUtils（登录令牌）
```

密码使用 BCrypt 哈希保存。JWT 在登录成功后生成，后续请求由 `LoginInterceptor` 解析。

### 社区模块

主要类：

- `BoardController`
- `BoardServiceImpl`
- `BoardMemberServiceImpl`
- `BoardMapper` / `BoardMemberMapper`
- `Board`、`BoardMember`
- `BoardVO`、`BriefBoardVO`、`SquareBoardVO`

主要职责：社区创建、社区搜索、社区广场、社区详情、社区管理、成员关系切换、封面上传和社区浏览记录。

社区成员角色在代码中定义为 `MEMBER`、`ADMIN`、`CREATOR`。社区管理权限由创建者或成员角色判断。

### 帖子模块

主要类：

- `PostController`
- `PostServiceImpl`
- `PostMapper`
- `Post`
- `PostVO`、`RecentPostVO`
- `PostCreateEvent`、`PostDeleteEvent`、`PostRestoreEvent`

主要职责：帖子创建、分页查询、详情查询、逻辑删除、恢复、浏览记录和点赞 / 评论数维护。

当前帖子删除使用 MyBatis-Plus 逻辑删除。恢复场景通过 Mapper 中的忽略逻辑删除查询和更新方法处理。

当前没有发现帖子编辑接口，也没有发现帖子专用图片上传接口。

### 评论模块

主要类：

- `CommentController`
- `CommentServiceImpl`
- `CommentMapper`
- `Comment`
- `CommentVO`

评论通过 `target`、`targetId`、`rootId`、`rootType` 和 `parentId` 表达评论关系。查询时先分页获取根评论，再批量查询子评论并组装树形结构。

评论创建后会发布评论事件，并通过 `MessageService` 异步发送站内消息。

### 评分模块

主要类：

- `RatingController`
- `RatingServiceImpl`
- `RatingMapper`
- `Rating`

评分使用 `action` 和当前状态计算目标状态，再通过状态差值 `delta` 更新帖子或评论的点赞数。评分记录由数据库唯一索引约束同一用户对同一目标的唯一关系。

### 消息模块

主要类：

- `MessageController`
- `MessageServiceImpl`
- `MessageMapper`
- `Message`
- `MessageVO`

消息由评论、点赞和点踩业务触发。`MessageServiceImpl.send` 使用 `@Async("taskExecutor")` 异步写入消息表，并在 Redis Hash 中维护用户未读数。

### 浏览记录模块

主要类：

- `BrowseRecordServiceImpl`
- `BrowseRecordMapper`
- `BrowseRecord`

浏览记录写入使用异步方法：Redis ZSet 保存有限数量的近期 ID，MySQL 保存访问流水。帖子最多保留 9 条，社区最多保留 5 条，这一限制由 `BrowseRecord.BrowseTarget` 定义。

### 文件上传模块

主要类：

- `FileUploadService`
- `WebConfig`
- `UserController` / `BoardController`

上传服务按日期和业务分类创建本地目录，使用 UUID 生成文件名，返回相对路径。`WebConfig` 将 `/uploads/**` 映射到配置项 `file.upload-path` 指向的物理目录。

当前复用场景包括用户头像和社区封面；未发现帖子正文图片上传的专用调用。

## 4. 数据库设计

### 数据库类型

- MySQL 8.x
- 数据库名：`forum_db`
- 字符集：初始化脚本主要使用 `utf8mb4`

### 主要表

| 表 | 用途 | 关键关系 |
| --- | --- | --- |
| `users` | 用户账号、资料和统计字段 | 被帖子、评论、评分、消息、社区引用 |
| `boards` | 社区板块信息和统计字段 | `creator` 指向用户 |
| `board_members` | 用户与社区的成员关系 | `board_id + member_id` 唯一 |
| `posts` | 帖子正文、所属社区和统计字段 | `creator` 指向用户，`board_id` 指向社区 |
| `comments` | 评论和多层评论关系 | `creator`、`target_id`、`root_id`、`parent_id` 表达关系 |
| `ratings` | 用户对帖子 / 评论等目标的评价 | `creator + target + target_id` 唯一 |
| `messages` | 站内通知 | `sender_id`、`receiver_id`、`target_id` 表达消息关系 |
| `browse_records` | 浏览流水 | `user_id + target + target_id` 表达访问记录 |

### 帖子表当前口径

- `title`：`varchar(100)`
- `content`：当前初始化脚本为 `mediumtext`
- `is_deleted`：逻辑删除标记
- `like_count`、`comment_count`：冗余计数，通过事件和原子更新维护

### 索引现状

当前 SQL 脚本中明确发现的非主键索引主要是：

- `board_members.uk_board_member(board_id, member_id)`
- `ratings.unique_user_like(creator, target, target_id)`

帖子、评论、浏览记录和消息表未发现与主要查询完全匹配的复合索引定义。后续进行性能优化时，应结合实际查询和执行计划设计索引，不应直接凭关键词堆叠索引。

## 5. 编码规范

以下内容基于当前代码总结，并区分已有做法和建议约束。

### 已有做法

- Java 包名使用小写，按职责分层。
- Controller 使用 `@RestController` 和 Spring MVC 映射注解。
- Service 使用接口 + `service.impl` 实现类。
- Mapper 接口继承 MyBatis-Plus `BaseMapper`，复杂查询放在 XML。
- Entity 使用 `@TableName`、`@TableId`、`@TableLogic` 等 MyBatis-Plus 注解。
- DTO 用于请求入参，VO 用于前端展示数据。
- 成功和失败响应统一使用 `Result`，分页数据使用 `PageResult`。
- 业务错误主要通过 `BusinessException` 抛出，由 `GlobalExceptionHandler` 统一转换。
- JWT 用户 ID 放入 `UserContext`，请求完成后由拦截器清理。
- 计数更新、消息发送、浏览记录使用 Spring Event、`@Async` 和线程池进行解耦。

### 需要遵守的维护建议

- 新增接口时优先复用现有 Controller -> Service -> Mapper 分层。
- 请求参数应新增 DTO，不要直接使用 Entity 承接复杂写入请求。
- 联表查询返回 VO，不要为了方便直接把数据库 Entity 暴露给前端。
- 修改数据库字段时必须同步检查 Entity、DTO、Mapper XML、前端类型和迁移脚本。
- 新增鉴权接口时明确它是必须认证还是 `@OptionalAuth` 可选认证。
- 删除、恢复等涉及权限的操作必须在 Service 层再次校验当前用户身份，不能只依赖前端按钮隐藏。
- 统一使用 `Result` / `PageResult`，不要在同一模块引入新的返回格式。
- 异步操作要明确失败后的影响；不要把需要强一致返回结果的核心写入无条件改为异步。
- 日志应记录必要的业务上下文，不记录密码、Token、验证码或其他敏感数据。
- 文件上传必须校验空文件、格式、大小和存储路径，不能仅依赖文件名后缀。

### 当前代码中需要注意的事实

- `JWTUtils` 当前在代码中包含固定签名密钥；新的知识库文档不记录该密钥。后续维护应改为外部配置并避免提交真实密钥。
- `application.yaml.example` 使用占位符，不应把真实数据库、Redis 或邮箱凭据写入文档。
- 部分代码仍存在直接 `System.out`、`printStackTrace` 和较多调试输出，日志规范尚未完全统一。
- 前端部分展示使用 `v-html` 渲染帖子内容；在引入富文本或图片标记前必须先明确安全过滤策略。

## 6. 后续 AI Coding 注意事项

未来 AI 修改本项目时，必须遵守以下规则：

1. 修改前先读取相关模块的 Controller、Service、Mapper、Entity、DTO / VO、前端 API 和调用页面。
2. 不扫描和修改无关模块，优先按一个可验证任务推进。
3. 不将当前单体项目擅自改造成微服务，不为了展示 MQ、ES 或 Spring Cloud 引入没有业务依据的基础设施。
4. 不直接覆盖用户已有的工作区改动；修改前先检查 `git status` 和相关文件 diff。
5. 数据库结构变更必须同时提供明确的迁移方式，并检查旧数据兼容性。
6. 新增接口必须说明鉴权要求、请求 DTO、返回 VO、错误处理和前端调用位置。
7. 逻辑删除、恢复、点赞数、评论数、消息和浏览记录等已有业务语义不能被破坏。
8. 不把开发 Agent 的自测清单当成最终测试结论；测试 Agent 应基于当前代码实际验证。
9. 代码修改完成后，至少执行相关模块的构建或测试，并明确哪些测试无法执行以及原因。
10. 涉及帖子正文、HTML、图片 URL 或文件上传时，必须优先考虑 XSS、路径穿越、文件类型伪造、资源失效和超限问题。

