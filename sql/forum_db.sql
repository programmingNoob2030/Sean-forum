/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80026 (8.0.26)
 Source Host           : localhost:3306
 Source Schema         : forum_db

 Target Server Type    : MySQL
 Target Server Version : 80026 (8.0.26)
 File Encoding         : 65001

 Date: 27/05/2026 21:01:12
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for board_members
-- ----------------------------
DROP TABLE IF EXISTS `board_members`;
CREATE TABLE `board_members`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `board_id` bigint NOT NULL COMMENT '所属社区ID',
  `member_id` bigint NOT NULL COMMENT '用户ID',
  `role` enum('MEMBER','ADMIN','CREATOR') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'MEMBER' COMMENT '成员的类型',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_board_member`(`board_id` ASC, `member_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for boards
-- ----------------------------
DROP TABLE IF EXISTS `boards`;
CREATE TABLE `boards`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '社区的ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '社区的名称',
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '社区的封面',
  `banner` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '社区的背景图',
  `member_count` int NULL DEFAULT 0 COMMENT '社区的成员数',
  `post_count` int NULL DEFAULT 0 COMMENT '社区的贴子数',
  `create_time` datetime NULL DEFAULT (now()) COMMENT '社区的创建时间',
  `type` enum('PRIVATE','PUBLIC','STRICT') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PUBLIC' COMMENT '社区的类型',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '社区的描述',
  `creator` bigint NOT NULL COMMENT '社区的创建者',
  `weekly_visitor` bigint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for browse_records
-- ----------------------------
DROP TABLE IF EXISTS `browse_records`;
CREATE TABLE `browse_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录的ID',
  `user_id` bigint NOT NULL COMMENT '用户的ID',
  `target` enum('POST','BOARD') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户访问的对象',
  `target_id` bigint NOT NULL COMMENT '用户访问的对象的ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '用户访问该对象的时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 179 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for comments
-- ----------------------------
DROP TABLE IF EXISTS `comments`;
CREATE TABLE `comments`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论的ID',
  `creator` int NOT NULL COMMENT '评论的发起人',
  `target` enum('COMMENT','USER','POST') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论的对象',
  `target_id` int NOT NULL COMMENT '评论对象的ID',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '评论的时间',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论的内容',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '评论的可见性',
  `root_id` bigint NULL DEFAULT 0 COMMENT '评论的根对象的ID',
  `like_count` int NULL DEFAULT 0 COMMENT '评论的喜欢数',
  `root_type` enum('COMMENT','USER','POST','EMPTY') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'EMPTY' COMMENT '评论的根对象的类型',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '顶级评论的ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 169 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for messages
-- ----------------------------
DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息的ID',
  `sender_id` bigint NULL DEFAULT NULL COMMENT '消息发起者的ID',
  `action` tinyint NULL DEFAULT NULL COMMENT '触发消息的行为',
  `target_id` bigint NULL DEFAULT NULL COMMENT '消息产生的载体的ID',
  `receiver_id` bigint NULL DEFAULT NULL COMMENT '消息接收者的ID',
  `is_read` tinyint NULL DEFAULT NULL COMMENT '消息是否已读',
  `content` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '展示消息时的固定文本',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消息的创建时间',
  `target` tinyint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 76 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for posts
-- ----------------------------
DROP TABLE IF EXISTS `posts`;
CREATE TABLE `posts`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '帖子的ID',
  `creator` bigint NOT NULL COMMENT '帖子的发布者',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '帖子的标题',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '帖子的内容',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '帖子的发布时间',
  `like_count` int NULL DEFAULT 0 COMMENT '帖子的喜欢数',
  `comment_count` int NULL DEFAULT 0 COMMENT '帖子的评论数',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '帖子的可见性',
  `board_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17815 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ratings
-- ----------------------------
DROP TABLE IF EXISTS `ratings`;
CREATE TABLE `ratings`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '喜欢的ID',
  `creator` int NOT NULL COMMENT '喜欢的人',
  `target` enum('COMMENT','USER','POST') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '喜欢的对象',
  `target_id` int NOT NULL COMMENT '喜欢对象的ID',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '喜欢的时间',
  `type` int NULL DEFAULT NULL COMMENT '喜欢的类型(1 0 -1)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unique_user_like`(`creator` ASC, `target` ASC, `target_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 80 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户的ID',
  `name` varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户的昵称',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户的密码',
  `register_time` date NOT NULL DEFAULT (now()) COMMENT '用户的注册时间',
  `last_login_time` datetime NOT NULL DEFAULT (now()) COMMENT '用户的上次登陆时间',
  `post_count` int NULL DEFAULT 0 COMMENT '用户的发帖数目',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户的邮箱',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户的头像',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
