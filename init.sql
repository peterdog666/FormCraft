CREATE DATABASE IF NOT EXISTS formcraft DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE formcraft;

CREATE TABLE IF NOT EXISTS `form_schema` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '表单ID',
  `name` VARCHAR(255) NOT NULL COMMENT '表单名称',
  `description` TEXT COMMENT '表单描述',
  `schema` TEXT COMMENT '表单字段定义JSON',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0草稿 1已发布',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='表单定义表';

CREATE TABLE IF NOT EXISTS `form_submission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '提交记录ID',
  `form_id` BIGINT NOT NULL COMMENT '表单ID',
  `data` TEXT COMMENT '提交的表单数据JSON',
  `submit_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `ip` VARCHAR(50) COMMENT '提交者IP地址',
  PRIMARY KEY (`id`),
  KEY `idx_form_id` (`form_id`),
  KEY `idx_submit_time` (`submit_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='表单提交记录表';
