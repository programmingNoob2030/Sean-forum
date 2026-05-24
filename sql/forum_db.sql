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

 Date: 24/05/2026 22:31:00
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
-- Records of board_members
-- ----------------------------
INSERT INTO `board_members` VALUES (1, 1, 17, 'CREATOR', '2026-05-09 15:12:05');
INSERT INTO `board_members` VALUES (2, 2, 17, 'CREATOR', '2026-05-09 15:29:07');
INSERT INTO `board_members` VALUES (3, 3, 17, 'CREATOR', '2026-05-09 15:30:47');
INSERT INTO `board_members` VALUES (4, 4, 17, 'CREATOR', '2026-05-09 15:31:20');
INSERT INTO `board_members` VALUES (5, 5, 18, 'CREATOR', '2026-05-11 18:37:31');

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
-- Records of boards
-- ----------------------------
INSERT INTO `boards` VALUES (1, 'GTA', '2026/05/09/board/cover/b42cb3b1fe6a4bd0bfb548900a704feb.png', NULL, 1, 1, NULL, 'PUBLIC', 'A community for Grand Theft Auto players and RockStar Games lovers', 17, 0);
INSERT INTO `boards` VALUES (2, 'RDR', '2026/05/09/board/cover/8fb0f2ca8a4d4a65b873ed26947b41cd.png', NULL, 1, 2, NULL, 'PUBLIC', 'A community for Red Dead Redemption players and RockStar Games lovers', 17, 0);
INSERT INTO `boards` VALUES (3, 'Bully', '2026/05/09/board/cover/64858bb0c74e4dda8206eac168b42852.png', NULL, 1, 1, NULL, 'PUBLIC', 'A community for Bully players and RockStar Games lovers', 17, 0);
INSERT INTO `boards` VALUES (4, 'RockStar', '2026/05/09/board/cover/7c1a8291e06a40d5acc7f0d98ca1d654.png', NULL, 1, 1, NULL, 'PUBLIC', 'A community for RockStar Games lovers', 17, 0);
INSERT INTO `boards` VALUES (5, 'Enligsh', '2026/05/11/board/cover/8128154a1e5d4c6895ff825d2fba79b0.jpg', NULL, 0, 1, '2026-05-11 18:37:31', 'PUBLIC', 'A community for Enligsh lovers', 18, 0);

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
) ENGINE = InnoDB AUTO_INCREMENT = 72 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of browse_records
-- ----------------------------
INSERT INTO `browse_records` VALUES (11, 17, 'BOARD', 1, '2026-05-14 10:17:09');
INSERT INTO `browse_records` VALUES (12, 17, 'BOARD', 2, '2026-05-14 10:17:13');
INSERT INTO `browse_records` VALUES (13, 17, 'BOARD', 1, '2026-05-14 10:17:15');
INSERT INTO `browse_records` VALUES (14, 17, 'BOARD', 2, '2026-05-14 10:18:54');
INSERT INTO `browse_records` VALUES (15, 17, 'BOARD', 1, '2026-05-14 10:19:00');
INSERT INTO `browse_records` VALUES (16, 17, 'BOARD', 2, '2026-05-14 10:19:01');
INSERT INTO `browse_records` VALUES (17, 17, 'BOARD', 1, '2026-05-14 10:19:03');
INSERT INTO `browse_records` VALUES (18, 17, 'POST', 17803, '2026-05-14 10:21:49');
INSERT INTO `browse_records` VALUES (19, 17, 'POST', 17803, '2026-05-14 10:21:51');
INSERT INTO `browse_records` VALUES (20, 17, 'POST', 17803, '2026-05-14 10:23:51');
INSERT INTO `browse_records` VALUES (21, 17, 'POST', 17803, '2026-05-14 10:23:53');
INSERT INTO `browse_records` VALUES (22, 17, 'POST', 17803, '2026-05-14 10:23:54');
INSERT INTO `browse_records` VALUES (23, 17, 'POST', 17803, '2026-05-14 10:23:55');
INSERT INTO `browse_records` VALUES (24, 17, 'POST', 17803, '2026-05-14 10:23:56');
INSERT INTO `browse_records` VALUES (25, 17, 'POST', 17803, '2026-05-14 10:23:57');
INSERT INTO `browse_records` VALUES (26, 17, 'POST', 17803, '2026-05-14 10:24:02');
INSERT INTO `browse_records` VALUES (27, 17, 'POST', 17804, '2026-05-14 10:24:09');
INSERT INTO `browse_records` VALUES (28, 17, 'POST', 17804, '2026-05-14 10:24:12');
INSERT INTO `browse_records` VALUES (29, 17, 'POST', 17804, '2026-05-14 10:24:15');
INSERT INTO `browse_records` VALUES (30, 17, 'POST', 17804, '2026-05-14 10:24:27');
INSERT INTO `browse_records` VALUES (31, 17, 'POST', 17804, '2026-05-14 10:53:43');
INSERT INTO `browse_records` VALUES (32, 17, 'POST', 17804, '2026-05-14 10:53:45');
INSERT INTO `browse_records` VALUES (33, 17, 'POST', 17803, '2026-05-14 10:53:46');
INSERT INTO `browse_records` VALUES (34, 17, 'POST', 17804, '2026-05-14 10:53:48');
INSERT INTO `browse_records` VALUES (35, 17, 'POST', 17803, '2026-05-14 10:53:49');
INSERT INTO `browse_records` VALUES (36, 17, 'POST', 17804, '2026-05-14 10:53:51');
INSERT INTO `browse_records` VALUES (37, 17, 'BOARD', 2, '2026-05-14 11:56:29');
INSERT INTO `browse_records` VALUES (38, 17, 'POST', 17804, '2026-05-14 11:56:32');
INSERT INTO `browse_records` VALUES (39, 17, 'POST', 17804, '2026-05-14 11:58:27');
INSERT INTO `browse_records` VALUES (40, 17, 'POST', 17804, '2026-05-14 12:00:01');
INSERT INTO `browse_records` VALUES (41, 17, 'POST', 17804, '2026-05-14 13:13:09');
INSERT INTO `browse_records` VALUES (42, 17, 'POST', 17803, '2026-05-14 13:13:14');
INSERT INTO `browse_records` VALUES (43, 17, 'POST', 17803, '2026-05-14 13:19:12');
INSERT INTO `browse_records` VALUES (44, 17, 'POST', 17804, '2026-05-14 13:19:14');
INSERT INTO `browse_records` VALUES (45, 17, 'POST', 17805, '2026-05-14 13:19:17');
INSERT INTO `browse_records` VALUES (46, 17, 'POST', 17803, '2026-05-14 13:19:50');
INSERT INTO `browse_records` VALUES (47, 17, 'POST', 17804, '2026-05-14 13:20:39');
INSERT INTO `browse_records` VALUES (48, 17, 'POST', 17803, '2026-05-14 13:20:41');
INSERT INTO `browse_records` VALUES (49, 17, 'POST', 17807, '2026-05-14 21:30:15');
INSERT INTO `browse_records` VALUES (50, 17, 'POST', 17807, '2026-05-14 21:42:15');
INSERT INTO `browse_records` VALUES (51, 17, 'POST', 17803, '2026-05-15 20:17:06');
INSERT INTO `browse_records` VALUES (52, 17, 'POST', 17804, '2026-05-23 20:01:21');
INSERT INTO `browse_records` VALUES (53, 17, 'POST', 17804, '2026-05-23 20:01:35');
INSERT INTO `browse_records` VALUES (54, 18, 'POST', 17804, '2026-05-23 20:03:11');
INSERT INTO `browse_records` VALUES (55, 17, 'POST', 17804, '2026-05-23 20:03:26');
INSERT INTO `browse_records` VALUES (56, 17, 'POST', 17804, '2026-05-23 20:08:32');
INSERT INTO `browse_records` VALUES (57, 17, 'POST', 17804, '2026-05-23 20:10:04');
INSERT INTO `browse_records` VALUES (58, 14, 'POST', 17804, '2026-05-23 20:12:50');
INSERT INTO `browse_records` VALUES (59, 17, 'POST', 17804, '2026-05-23 20:20:10');
INSERT INTO `browse_records` VALUES (60, 17, 'POST', 17804, '2026-05-23 20:20:23');
INSERT INTO `browse_records` VALUES (61, 18, 'POST', 17803, '2026-05-24 20:10:52');
INSERT INTO `browse_records` VALUES (62, 17, 'POST', 17808, '2026-05-24 20:13:21');
INSERT INTO `browse_records` VALUES (63, 17, 'POST', 17808, '2026-05-24 20:14:07');
INSERT INTO `browse_records` VALUES (64, 18, 'POST', 17808, '2026-05-24 20:42:47');
INSERT INTO `browse_records` VALUES (65, 17, 'POST', 17803, '2026-05-24 21:38:59');
INSERT INTO `browse_records` VALUES (66, 18, 'POST', 17804, '2026-05-24 21:42:18');
INSERT INTO `browse_records` VALUES (67, 18, 'POST', 17805, '2026-05-24 21:42:22');
INSERT INTO `browse_records` VALUES (68, 18, 'POST', 17808, '2026-05-24 21:42:26');
INSERT INTO `browse_records` VALUES (69, 18, 'POST', 17808, '2026-05-24 21:42:36');
INSERT INTO `browse_records` VALUES (70, 17, 'POST', 17803, '2026-05-24 21:48:59');
INSERT INTO `browse_records` VALUES (71, 17, 'POST', 17804, '2026-05-24 21:49:07');

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
) ENGINE = InnoDB AUTO_INCREMENT = 115 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comments
-- ----------------------------
INSERT INTO `comments` VALUES (102, 14, 'POST', 19, '2026-04-25 12:44:55', '我李四来评论一句', 0, 19, 0, 'POST', 0);
INSERT INTO `comments` VALUES (103, 13, 'COMMENT', 102, '2026-04-25 12:45:22', '你算个几把，你还评论上了', 0, 19, 0, 'POST', 102);
INSERT INTO `comments` VALUES (104, 14, 'COMMENT', 103, '2026-04-25 12:45:49', '回复 @zhangsan: 我草泥马，你逼逼尼玛呢', 0, 19, 0, 'POST', 102);
INSERT INTO `comments` VALUES (105, 13, 'COMMENT', 103, '2026-04-25 12:49:10', '回复 @zhangsan: 吵你妈呢吵', 0, 19, 0, 'POST', 102);
INSERT INTO `comments` VALUES (106, 18, 'POST', 17803, '2026-05-11 19:15:18', 'Hello zhaoliu', 0, 17803, 0, 'POST', 0);
INSERT INTO `comments` VALUES (107, 17, 'COMMENT', 106, '2026-05-13 11:11:53', 'hello', 0, 17803, 0, 'POST', 106);
INSERT INTO `comments` VALUES (108, 17, 'POST', 17804, '2026-05-23 20:01:35', '你好窥谷忘反', 0, 17804, 0, 'POST', 0);
INSERT INTO `comments` VALUES (109, 17, 'POST', 17804, '2026-05-23 20:08:31', '你好望峰息心', 0, 17804, 0, 'POST', 0);
INSERT INTO `comments` VALUES (110, 17, 'POST', 17804, '2026-05-23 20:10:03', '你好鸢飞戾天', 0, 17804, 0, 'POST', 0);
INSERT INTO `comments` VALUES (111, 14, 'COMMENT', 108, '2026-05-23 20:13:03', '你在背书吗', 0, 17804, 0, 'POST', 108);
INSERT INTO `comments` VALUES (112, 17, 'COMMENT', 111, '2026-05-23 20:20:33', '回复 @lisi: 没有哦，我在测试新功能', 0, 17804, 0, 'POST', 108);
INSERT INTO `comments` VALUES (113, 17, 'POST', 17808, '2026-05-24 20:14:07', '你好，窥谷忘反', 0, 17808, 1, 'POST', 0);
INSERT INTO `comments` VALUES (114, 18, 'COMMENT', 113, '2026-05-24 20:42:55', '你好赵六', 0, 17808, 1, 'POST', 113);

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
  `create_time` datetime NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP COMMENT '消息的创建时间',
  `target` tinyint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of messages
-- ----------------------------
INSERT INTO `messages` VALUES (4, 17, 3, 17808, 18, NULL, '你好，窥谷忘反', NULL, 1);
INSERT INTO `messages` VALUES (5, 18, 3, 113, 17, NULL, '你好赵六', '2026-05-24 20:42:55', 2);
INSERT INTO `messages` VALUES (6, 17, 1, 17804, 18, NULL, NULL, '2026-05-24 21:39:12', 1);
INSERT INTO `messages` VALUES (7, 17, 1, 17808, 18, NULL, NULL, '2026-05-24 21:39:15', 1);
INSERT INTO `messages` VALUES (8, 18, 1, 113, 17, NULL, NULL, '2026-05-24 21:42:37', 2);
INSERT INTO `messages` VALUES (9, 17, 2, 17804, 18, NULL, NULL, '2026-05-24 21:49:07', 1);

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
) ENGINE = InnoDB AUTO_INCREMENT = 17809 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of posts
-- ----------------------------
INSERT INTO `posts` VALUES (17803, 17, 'd', 'd', '2026-05-11 14:49:47', -1, 2, 0, 2);
INSERT INTO `posts` VALUES (17804, 18, 'A test title in English Board', 'test content', '2026-05-11 18:38:34', -1, 5, 0, 5);
INSERT INTO `posts` VALUES (17805, 17, 'ddd', 'dddd', '2026-05-14 09:23:52', 0, 0, 0, 4);
INSERT INTO `posts` VALUES (17806, 17, 'wocao', 'dwadaw', '2026-05-14 09:24:00', 0, 0, 0, 1);
INSERT INTO `posts` VALUES (17807, 17, 'idoas', 'dsad', '2026-05-14 09:24:09', 0, 0, 0, 3);
INSERT INTO `posts` VALUES (17808, 18, '我在rdr的社区下面的标题', '我在rdr的社区下的内容', '2026-05-24 20:13:06', 1, 2, 0, 2);

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
) ENGINE = InnoDB AUTO_INCREMENT = 75 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ratings
-- ----------------------------
INSERT INTO `ratings` VALUES (59, 14, 'POST', 19, '2026-04-25 12:44:45', 0);
INSERT INTO `ratings` VALUES (60, 13, 'COMMENT', 104, '2026-04-25 12:46:12', -1);
INSERT INTO `ratings` VALUES (61, 13, 'COMMENT', 103, '2026-04-25 12:46:13', -1);
INSERT INTO `ratings` VALUES (62, 14, 'COMMENT', 103, '2026-04-25 12:46:31', 1);
INSERT INTO `ratings` VALUES (63, 14, 'COMMENT', 104, '2026-04-25 12:46:31', 1);
INSERT INTO `ratings` VALUES (64, 18, 'POST', 20, '2026-04-29 21:10:47', 0);
INSERT INTO `ratings` VALUES (65, 17, 'POST', 17791, '2026-05-08 17:47:26', 1);
INSERT INTO `ratings` VALUES (66, 17, 'POST', 8911, '2026-05-08 17:47:33', 0);
INSERT INTO `ratings` VALUES (67, 17, 'POST', 8916, '2026-05-08 17:47:42', 0);
INSERT INTO `ratings` VALUES (68, 14, 'POST', 20, '2026-05-09 16:54:07', 0);
INSERT INTO `ratings` VALUES (69, 17, 'POST', 17806, '2026-05-16 21:35:26', 0);
INSERT INTO `ratings` VALUES (70, 17, 'POST', 17803, '2026-05-24 21:39:03', -1);
INSERT INTO `ratings` VALUES (71, 17, 'POST', 17804, '2026-05-24 21:39:12', -1);
INSERT INTO `ratings` VALUES (72, 17, 'POST', 17808, '2026-05-24 21:39:15', 1);
INSERT INTO `ratings` VALUES (73, 18, 'COMMENT', 114, '2026-05-24 21:42:28', 1);
INSERT INTO `ratings` VALUES (74, 18, 'COMMENT', 113, '2026-05-24 21:42:37', 1);

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

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (14, 'lisi', '$2a$10$xBszahNCD9/6E/4DmWN3j.kwfWOOSMizRGjESJJ3PlktfkukpibuW', '2026-04-24', '2026-05-23 20:20:44', 0, 'lisi@sasu.com', '2026/05/09/user/avatar/d6cf724c35214ca9800756c388c4da62.png');
INSERT INTO `users` VALUES (15, 'wangwu', '$2a$10$fgE0nU8soMs8nHs1JCZRxuYyY9K8R81P4OI0m93PD.ytfyAuK80Mq', '2026-04-25', '2026-04-25 18:37:08', 0, 'wangwu@sasu.com', NULL);
INSERT INTO `users` VALUES (17, 'zhaoliu', '$2a$10$jxk4nWnEDrw50qYFaygHZe/JL.sYtBQvwTTVq9TtpQ8rNWC.I7xkS', '2026-04-28', '2026-05-24 21:42:47', 4, 'zhaoliu@sasu.com', '2026/05/11/user/avatar/b4d8449ded1a498ebcfe3b5e65e6db19.png');
INSERT INTO `users` VALUES (18, '窥谷忘反', '$2a$10$q95D3UgbTLuaRMaGlk4eDOXsvyLEL5xQNHCe5oryb6G2KA1tbaR3O', '2026-04-28', '2026-05-24 21:49:25', 2, '3567844414@qq.com', '2026/05/11/user/avatar/f129fbca418a423e844f41d561742bb1.jpg');

SET FOREIGN_KEY_CHECKS = 1;
