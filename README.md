

# Sean's Forum - 基于 Spring Boot 3 + Vue3 的轻量级社区论坛系统

## 📖 项目简介

Sean's Forum 是一个前后端分离的轻量级社区论坛系统，围绕用户注册登录、板块创建与浏览、帖子发布、评论互动、点赞/点踩、站内消息和最近浏览记录等核心社区场景展开。

项目后端基于 Spring Boot 3 构建 RESTful API，使用 MyBatis-Plus 操作 MySQL 完成核心业务数据持久化，并通过 Redis 管理邮箱验证码、登录会话和浏览记录等状态数据。前端基于 Vue3 + Element Plus 实现页面交互与接口联调，形成了从用户认证、内容发布到互动通知的完整业务闭环。

------

## 🛠️ 技术栈

- **前端框架**:   Vue3、Vite、Element Plus、Vue Router、Pinia、Axios
- **后端框架**：Spring Boot 3、MyBatis-Plus
- **数据存储**：MySQL、Redis
- **身份验证**：JWT + jBCrypt 
- **文件存储**：本地文件系统上传 (可扩展为 OSS)
- **工具类**：Lombok, PageHelper (分页插件), Jakarta Validation (参数校验)
- **邮件服务**：Spring Boot Mail (基于 SMTP 协议)

------

## 🏗️ 项目目录布局

项目采用前后端分离目录结构，工程结构如下：

前端

```
frontend
├── src/
│   ├── api/                  # 接口请求封装
│   ├── assets/               # 静态资源（如：default_avatar.svg）
│   ├── components/           # 业务子组件（如：message/MessageItem.vue）
│   ├── models/ 			  # 类型定义与 Pinia 状态管理
│   ├── router/               # 路由映射与路由守卫
│   ├── utils/                # 工具函数（如：axios封装、Token管理）
│   ├── views/                # 页面级组件/路由入口（如：users/login.vue）
│   ├── App.vue               # 顶层根组件
│   ├── main.ts               # 项目入口文件
│   └── style.css             # 全局基础样式
└── index.html                # SPA 主 HTML 模板
```

后端

```
backend
├── src/main/java/sim/forum
│   ├── annotation/        # 自定义注解（如：@OptionalAuth）
│   ├── config/            # 框架配置（Redis, MVC, MyBatis Plus）
│   ├── context/           # 全局上下文（如：UserContext 基于 ThreadLocal）
│   ├── controller/        # RESTful API 控制器层
│   ├── dto/               # 数据传输对象（接收前端参数）
│   ├── entity/            # 数据库实体类
│   ├── event/             # 事件监听/发布逻辑
│   ├── exception/         # 全局异常处理器
│   ├── interceptor/       # 拦截器（JWT 校验、权限控制）
│   ├── listener/          # Spring 监听器
│   ├── mapper/            # MyBatis Mapper 接口
│   ├── result/            # 统一响应封装（Result, PageResult）
│   ├── service/           # 业务逻辑层接口及实现
│   ├── utils/             # 常用工具类
│   ├── vo/                # 视图对象（返回给前端的脱敏数据）
│   └── ForumApplication.java # 项目启动类
└── src/main/resources
    ├── mapper/            # MyBatis XML 映射文件
    └── application.yaml.example   # 配置文件示例
```

------

## 🌟 核心功能模块

### 1. 用户中心 (User Module)

- **身份管理**：支持用户注册、基于密码的登录验证（使用 BCrypt 加密保存）。
- **安全校验**：集成 163 邮箱 SMTP 服务，实现邮箱有效性检查与验证码校验。
- **个人信息**：支持用户头像上传（MultipartFile 处理）及基本资料更新。

### 2. 板块管理 (Board Module)

- **创建与发现**：用户可创建社区板块，系统支持通过关键词搜索板块。
- **板块广场**：提供广场视图，展示推荐或热门板块。
- **成员关联**：追踪用户加入或创建的板块。

### 3. 内容发布 (Post Module)

- **发布逻辑**：支持板块内的帖子创建，具备自动关联当前登录用户的功能。
- **生命周期管理**：支持帖子的逻辑删除与物理恢复。
- **分页检索**：高性能的分页查询，支持按需加载帖子列表。

### 4. 互动评价 (Comment & Rating)

- **多层评论**：支持针对帖子的评论发表、删除与恢复。
- **投票系统**：支持点赞/踩（Toggle 机制），基于 `RatingDTO` 实现高效的评价切换。

------

## 🚀 快速开始

### 环境要求

- **JDK**：17+
- **Maven**：3.8+
- **Node.js**：18+
- **MySQL**：8.0+
- **Redis**：6.0+

### 数据库配置

1. 创建名为 `forum_db` 的数据库。

```SQL
CREATE DATABASE forum_db CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```
执行项目根目录 sql/ 下的 forum_db.sql，初始化表结构及必要的基础数据。

### 后端启动

2. 将 `backend/src/main/resources/application.yaml.example` 复制为 `application.yaml`。
3. 修改 `application.yaml` 中的 MySQL、Redis、邮箱等配置。
4. 启动 MySQL 和 Redis。
5. 进入后端目录启动服务：

```bash
cd backend
mvn spring-boot:run
```
### 前端启动

```bash
cd frontend
npm install
npm run dev
```

启动后访问终端输出的 Vite 本地地址(http://localhost:5173)。

------

## 📑 API 接口预览

| **模块** | **路由**         | **方法** | **功能描述**                     |
| -------- | ---------------- | -------- | -------------------------------- |
| **用户** | `/users`         | `POST`   | 用户注册                         |
|          | `/session`       | `POST`   | 用户登录（获取 JWT 及个人信息）  |
|          | `/email`         | `GET`    | 校验邮箱可用性/是否已注册        |
|          | `/code`          | `GET`    | 校验邮箱验证码准确性             |
|          | `/password`      | `PUT`    | 重置用户密码                 |
|          | `/info`          | `PUT`    | 更新用户资料（如昵称等）         |
|          | `/user/avatar`   | `POST`   | 上传/更新个人头像                |
| **社区** | `/boards`        | `POST`   | 创建新社区                       |
|          | `/boards`        | `GET`    | 获取社区广场列表                 |
|          | `/boards/mine`   | `GET`    | 获取我加入/创建的社区            |
|          | `/board/{id}`    | `GET`    | 获取特定社区详情                 |
|          | `/board/search`  | `GET`    | 根据关键字搜索社区               |
|          | `/board/cover`   | `POST`   | 上传/更换社区封面图              |
|          | `/boards/history`   | `GET`   |  **获取最近浏览的社区**(5条之内)        |
|          | `/boards/history`   | `DELETE`   | **删除最近浏览的社区记录**         |
| **帖子** | `/posts`         | `POST`   | 在指定社区下发布新帖子      |
|          | `/posts`         | `GET`    | 分页获取帖子列表（支持可选认证） |
|          | `/post/{id}`     | `GET`    | 获取帖子正文及详细信息           |
|          | `/posts`         | `DELETE` | 逻辑删除帖子                 |
|          | `/posts`         | `PATCH`  | 恢复已删除的帖子             |
|		   | `/posts/history` | `GET`    | **获取最近浏览的帖子**(9条之内) |
|		   | `/posts/history` | `DELETE`    | **删除最近浏览的帖子记录** |
| **评论** | `/post-comments` | `GET`    | 分页获取指定帖子下的评论列表     |
|          | `/comments`      | `POST`   | 发表新评论                       |
|          | `/comments`      | `DELETE` | 逻辑删除评论                 |
|          | `/comments`      | `PATCH`  | 恢复已删除的评论         |
| **评分** | `/ratings`       | `PUT`    | Toggle 机制：切换点赞/踩状态 |
| **消息**|`/message/unread-count`|`GET`   |获取用户**未读**消息数|
||`/messages`|`GET`|获取用户全部消息| 



## 🛡️ 安全性说明

1. **密码存储**：使用 `jBCrypt` 进行高强度加盐哈希存储，防止拖库。
2. **身份令牌**：采用 `JWT` 机制，配合 `ThreadLocal` 在 `UserContext` 中管理用户态。
3. **参数校验**：全量使用 `Jakarta Validation` 对 Controller 输入进行严格拦截。
4. **会话持久化**：使用 `Spring Session Redis` 实现分布式环境下会话的一致性。

## 🚧 待办事项 (Roadmap)

- **权限加固 (Security)**：基于 `Interceptor` 实现管理员/用户角色校验，保护逻辑删除等敏感接口。
- **缓存进阶 (Redis)**：利用 Redis 实现“热门帖子排行”与“用户最近浏览足迹”，提升响应速度。(初步完成)
- **异步解耦 (Spring Event)**：利用异步事件处理评论、点赞后的“站内信”通知，优化发帖性能。(初步完成)
- **SQL 调优 (Optimization)**：针对核心业务表建立复合索引，优化 `LIKE` 查询与大分页查询效率。
- **工程交付 (DevOps)**：完善数据库初始化脚本与项目部署手册，提升项目交付标准。(与时俱进)