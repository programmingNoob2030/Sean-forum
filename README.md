# Forum System Backend (Sean-Forum)

## 📖 项目简介

本项目是一个基于 **Java 17** 和 **Spring Boot 3** 架构的现代化社区论坛系统。项目采用前后端分离模式，核心设计关注数据一致性、高并发下的缓存策略以及用户交互体验。系统集成了用户身份验证、板块管理、帖子发布、层级评论、以及基于 Redis 的会话管理等核心功能，适用于构建各类垂直社区或兴趣论坛。

------

## 🛠️ 技术栈

项目采用了当前主流的后端技术栈，确保系统的稳定与可扩展性：

- **核心框架**：Spring Boot 3.5.11
- **持久层**：MyBatis Plus 3.5.9 (配合 MySQL 驱动)
- **数据库**：MySQL 8.0+
- **缓存/会话**：Redis (Spring Data Redis + Spring Session)
- **身份验证**：JWT (java-jwt 4.4.0) + jBCrypt (加盐哈希加密)
- **文件存储**：本地文件系统上传 (可扩展为 OSS)
- **工具类**：Lombok, PageHelper (分页插件), Jakarta Validation (参数校验)
- **邮件服务**：Spring Boot Mail (基于 SMTP 协议)

------

## 🏗️ 项目目录布局

根据提供的项目截图及 Maven 标准，项目工程结构如下：

Plaintext

```
forum
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
    └── application.yaml   # 核心配置文件
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

- **JDK**: 17+
- **Maven**: 3.8+
- **MySQL**: 8.0+(推荐)
- **Redis**: 6.0+

### 数据库配置

1. 创建名为 `forum_db` 的数据库。

2. 修改 `src/main/resources/application.yaml` 中的连接信息：

   YAML

   ```
   datasource:
     url: jdbc:mysql://localhost:3306/forum_db?serverTimezone=Asia/Shanghai
     username: your_username
     password: your_password
   ```
2. 数据库初始化 (核心步骤)
手动创建名为 forum_db 的数据库：

```SQL
CREATE DATABASE forum_db CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```
执行脚本：定位到项目根目录下的 sql/ 文件夹（或你存放脚本的位置），执行 forum_db.sql 以初始化表结构及必要的基础数据。

### 运行步骤

1. 克隆项目并进入根目录。
2. 执行 Maven 构建：`mvn clean install`。
3. 启动 Redis 服务。
4. 运行 `ForumApplication.java` 中的 `main` 方法。

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



## 🛡️ 安全性说明

1. **密码存储**：使用 `jBCrypt` 进行高强度加盐哈希存储，防止拖库。
2. **身份令牌**：采用 `JWT` 机制，配合 `ThreadLocal` 在 `UserContext` 中管理用户态。
3. **参数校验**：全量使用 `Jakarta Validation` 对 Controller 输入进行严格拦截。
4. **会话持久化**：使用 `Spring Session Redis` 实现分布式环境下会话的一致性。



## 🚧 待办事项 (Roadmap)

- **权限加固 (Security)**：基于 `Interceptor` 实现管理员/用户角色校验，保护逻辑删除等敏感接口。
- **缓存进阶 (Redis)**：利用 Redis 实现“热门帖子排行”与“用户最近浏览足迹”，提升响应速度。
- **异步解耦 (Spring Event)**：利用异步事件处理评论、点赞后的“站内信”通知，优化发帖性能。
- **SQL 调优 (Optimization)**：针对核心业务表建立复合索引，优化 `LIKE` 查询与大分页查询效率。
- **工程交付 (DevOps)**：完善数据库初始化脚本与项目部署手册，提升项目交付标准。