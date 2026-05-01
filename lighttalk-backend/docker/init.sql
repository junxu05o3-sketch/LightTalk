-- =============================================
-- LightTalk 数据库初始化脚本
-- 数据库: lighttalk
-- 字符集: utf8mb4
-- 排序规则: utf8mb4_unicode_ci
-- =============================================

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- =============================================
-- 1. 用户表 (user)
-- 存储用户基本信息和状态
-- =============================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    `nickname` VARCHAR(50) NOT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '用户状态: 1-正常, 0-禁言',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 2. 房间表 (room)
-- 存储聊天房间信息
-- =============================================
DROP TABLE IF EXISTS `room`;
CREATE TABLE `room` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '房间ID',
    `name` VARCHAR(100) NOT NULL COMMENT '房间名称',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '房间描述',
    `owner_id` BIGINT NOT NULL COMMENT '房主用户ID',
    `max_members` INT NOT NULL DEFAULT 100 COMMENT '最大成员数',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '房间状态: 1-正常, 0-已关闭',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房间表';

-- =============================================
-- 3. 房间成员表 (room_member)
-- 存储用户与房间的关联关系
-- =============================================
DROP TABLE IF EXISTS `room_member`;
CREATE TABLE `room_member` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `room_id` BIGINT NOT NULL COMMENT '房间ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '房间内昵称',
    `role` TINYINT NOT NULL DEFAULT 0 COMMENT '角色: 0-普通成员, 1-房主, 2-管理员',
    `join_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_room_user` (`room_id`, `user_id`),
    KEY `idx_room_id` (`room_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_join_time` (`join_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房间成员表';

-- =============================================
-- 4. 消息表 (message)
-- 存储聊天消息记录
-- =============================================
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `room_id` BIGINT NOT NULL COMMENT '房间ID',
    `user_id` BIGINT NOT NULL COMMENT '发送者用户ID',
    `nickname` VARCHAR(50) NOT NULL COMMENT '发送者昵称(冗余存储)',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `type` VARCHAR(20) NOT NULL DEFAULT 'text' COMMENT '消息类型: text-文本, system-系统消息',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_room_id` (`room_id`),
    KEY `idx_user_id` (`user_id`),
    -- 复合索引: 优化按房间查询历史消息的分页性能
    KEY `idx_room_create_time` (`room_id`, `create_time`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- =============================================
-- 初始化测试数据 (可选)
-- =============================================

-- 插入测试用户 (密码为 BCrypt 加密的 '123456')
INSERT INTO `user` (`username`, `password`, `nickname`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '管理员', 1),
('test', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '测试用户', 1);

-- 插入测试房间
INSERT INTO `room` (`name`, `description`, `owner_id`, `status`) VALUES
('cpp', 'C++技术交流群', 1, 1),
('java', 'Java技术交流群', 1, 1);

-- 插入测试房间成员
INSERT INTO `room_member` (`room_id`, `user_id`, `nickname`, `role`) VALUES
(1, 1, '管理员', 1),
(2, 1, '管理员', 1);

-- 插入测试消息
INSERT INTO `message` (`room_id`, `user_id`, `nickname`, `content`, `type`) VALUES
(1, 1, '管理员', '欢迎来到C++技术交流群', 'system'),
(1, 1, '管理员', '大家好，欢迎交流C++相关技术问题', 'text');
