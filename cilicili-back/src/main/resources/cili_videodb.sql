/*
 Navicat Premium Data Transfer

 Source Server         : zsn
 Source Server Type    : MySQL
 Source Server Version : 80034
 Source Host           : localhost:3306
 Source Schema         : cili_videodb

 Target Server Type    : MySQL
 Target Server Version : 80034
 File Encoding         : 65001

 Date: 26/06/2026 00:13:38
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `video_id` bigint(0) UNSIGNED NOT NULL COMMENT '所属视频ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '评论者用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `parent_id` bigint(0) UNSIGNED NULL DEFAULT NULL COMMENT '父评论ID，NULL表示一级评论',
  `reply_to_id` bigint(0) UNSIGNED NULL DEFAULT NULL COMMENT '回复的目标评论ID',
  `reply_to_uid` bigint(0) UNSIGNED NULL DEFAULT NULL COMMENT '回复的目标用户ID',
  `like_count` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '点赞数',
  `floor` int(0) UNSIGNED NULL DEFAULT NULL COMMENT '楼层号（仅一级评论有效）',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_video_parent`(`video_id`, `parent_id`) USING BTREE,
  INDEX `idx_video_created`(`video_id`, `created_at`) USING BTREE,
  INDEX `idx_user`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for comment_like
-- ----------------------------
DROP TABLE IF EXISTS `comment_like`;
CREATE TABLE `comment_like`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT,
  `comment_id` bigint(0) UNSIGNED NOT NULL,
  `user_id` bigint(0) UNSIGNED NOT NULL,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_comment_user`(`comment_id`, `user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论点赞记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for danmaku
-- ----------------------------
DROP TABLE IF EXISTS `danmaku`;
CREATE TABLE `danmaku`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '弹幕ID',
  `video_id` bigint(0) UNSIGNED NOT NULL COMMENT '所属视频ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '发送者用户ID',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '弹幕内容',
  `play_time` int(0) UNSIGNED NOT NULL COMMENT '弹幕出现时间(秒)',
  `color` varchar(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '#FFFFFF' COMMENT '弹幕颜色(十六进制)',
  `mode` tinyint(0) NOT NULL DEFAULT 1 COMMENT '弹幕模式：1滚动，2顶部，3底部',
  `font_size` tinyint(0) UNSIGNED NOT NULL DEFAULT 16 COMMENT '字体大小',
  `send_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_video_time`(`video_id`, `play_time`) USING BTREE,
  INDEX `idx_video_sendtime`(`video_id`, `send_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '弹幕表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for follow
-- ----------------------------
DROP TABLE IF EXISTS `follow`;
CREATE TABLE `follow`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT,
  `follower_id` bigint(0) UNSIGNED NOT NULL COMMENT '粉丝ID',
  `followee_id` bigint(0) UNSIGNED NOT NULL COMMENT '被关注者ID',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_follower_followee`(`follower_id`, `followee_id`) USING BTREE,
  INDEX `idx_followee`(`followee_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户关注表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tag
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标签名称',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_name`(`name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '标签表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密后的密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `signature` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '这个人很神秘，什么也没有留下' COMMENT '个性签名',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态：0封禁，1正常，2申请注销',
  `role` tinyint(0) NULL DEFAULT 0 COMMENT '是否为管理员：1是，0否',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE,
  UNIQUE INDEX `uk_email`(`email`) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for video
-- ----------------------------
DROP TABLE IF EXISTS `video`;
CREATE TABLE `video`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '视频ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '视频标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '视频简介',
  `cover_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图片URL',
  `video_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '视频文件URL (MP4)',
  `duration` int(0) UNSIGNED NULL DEFAULT 0 COMMENT '视频时长(秒)',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态：1正常，0下架，2审核中',
  `play_count` bigint(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '播放量',
  `danmaku_count` bigint(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '弹幕总数',
  `comment_count` bigint(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '评论总数',
  `like_count` bigint(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '点赞数',
  `coin_count` bigint(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '投币数',
  `collect_count` bigint(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '收藏数',
  `share_count` bigint(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '分享数',
  `uploader_id` bigint(0) UNSIGNED NOT NULL COMMENT '上传者用户ID',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_uploader`(`uploader_id`) USING BTREE,
  INDEX `idx_status_created`(`status`, `created_at`) USING BTREE,
  INDEX `idx_play_count`(`play_count`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '视频表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for video_coin
-- ----------------------------
DROP TABLE IF EXISTS `video_coin`;
CREATE TABLE `video_coin`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT,
  `video_id` bigint(0) UNSIGNED NOT NULL,
  `user_id` bigint(0) UNSIGNED NOT NULL,
  `num` tinyint(0) UNSIGNED NOT NULL DEFAULT 1 COMMENT '投币数量(1或2)',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_video_user`(`video_id`, `user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '视频投币记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for video_collect
-- ----------------------------
DROP TABLE IF EXISTS `video_collect`;
CREATE TABLE `video_collect`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT,
  `video_id` bigint(0) UNSIGNED NOT NULL,
  `user_id` bigint(0) UNSIGNED NOT NULL,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_video_user_collect`(`video_id`, `user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '视频收藏记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for video_like
-- ----------------------------
DROP TABLE IF EXISTS `video_like`;
CREATE TABLE `video_like`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT,
  `video_id` bigint(0) UNSIGNED NOT NULL,
  `user_id` bigint(0) UNSIGNED NOT NULL,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_video_user_like`(`video_id`, `user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '视频点赞记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for video_tag
-- ----------------------------
DROP TABLE IF EXISTS `video_tag`;
CREATE TABLE `video_tag`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT,
  `video_id` bigint(0) UNSIGNED NOT NULL,
  `tag_id` bigint(0) UNSIGNED NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_video_tag`(`video_id`, `tag_id`) USING BTREE,
  INDEX `idx_tag_id`(`tag_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '视频标签关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for watch_history
-- ----------------------------
DROP TABLE IF EXISTS `watch_history`;
CREATE TABLE `watch_history`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) UNSIGNED NOT NULL,
  `video_id` bigint(0) UNSIGNED NOT NULL,
  `progress` int(0) UNSIGNED NULL DEFAULT 0 COMMENT '观看进度(秒)',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_video`(`user_id`, `video_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '观看历史' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
