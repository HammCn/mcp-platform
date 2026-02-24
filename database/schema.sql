-- =====================================================
-- MCP Platform 数据库建表脚本
-- MySQL 8.0+
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS mcp_platform 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE mcp_platform;

-- =====================================================
-- 1. MCP 服务定义表
-- =====================================================
DROP TABLE IF EXISTS `mcp_service`;
CREATE TABLE `mcp_service` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `service_code` VARCHAR(64) NOT NULL COMMENT '服务编码（唯一标识）',
    `service_name` VARCHAR(128) NOT NULL COMMENT '服务名称',
    `description` VARCHAR(1024) COMMENT '服务描述',
    `version` VARCHAR(32) COMMENT '服务版本',
    `service_type` VARCHAR(32) NOT NULL COMMENT '服务类型：HTTP, GRPC, WEBSOCKET',
    `base_url` VARCHAR(512) COMMENT '基础 URL',
    `openapi_spec` TEXT COMMENT 'OpenAPI 规范 URL 或内容',
    `spec_type` VARCHAR(32) COMMENT 'OpenAPI 规范类型：URL, CONTENT',
    `auth_type` VARCHAR(32) COMMENT '认证类型：API_KEY, JWT, OAUTH2, NONE',
    `auth_config` TEXT COMMENT '认证配置（JSON 格式）',
    `health_check_path` VARCHAR(256) COMMENT '健康检查路径',
    `status` VARCHAR(32) NOT NULL COMMENT '服务状态：REGISTERED, HEALTHY, UNHEALTHY, DISABLED',
    `mcp_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用 MCP 协议转换',
    `mcp_tool_prefix` VARCHAR(64) COMMENT 'MCP 工具前缀',
    `timeout_ms` INT COMMENT '超时时间（毫秒）',
    `retry_count` INT COMMENT '重试次数',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` VARCHAR(64) COMMENT '创建人',
    `updated_by` VARCHAR(64) COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_service_code` (`service_code`),
    KEY `idx_service_status` (`status`),
    KEY `idx_service_type` (`service_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MCP 服务定义表';

-- =====================================================
-- 2. MCP 服务实例表
-- =====================================================
DROP TABLE IF EXISTS `mcp_service_instance`;
CREATE TABLE `mcp_service_instance` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `service_id` BIGINT NOT NULL COMMENT '服务 ID',
    `instance_address` VARCHAR(256) NOT NULL COMMENT '实例地址（IP:Port）',
    `metadata` TEXT COMMENT '实例元数据（JSON 格式）',
    `weight` INT NOT NULL DEFAULT 100 COMMENT '权重（用于负载均衡）',
    `health_status` VARCHAR(32) NOT NULL DEFAULT 'HEALTHY' COMMENT '健康状态：HEALTHY, UNHEALTHY',
    `last_health_check_at` DATETIME COMMENT '最后健康检查时间',
    `last_health_check_result` VARCHAR(1024) COMMENT '最后健康检查结果',
    `consecutive_failures` INT NOT NULL DEFAULT 0 COMMENT '连续健康检查失败次数',
    `status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '实例状态：ACTIVE, INACTIVE',
    `registered_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deregistered_at` DATETIME COMMENT '注销时间',
    PRIMARY KEY (`id`),
    KEY `idx_service_id` (`service_id`),
    KEY `idx_instance_address` (`instance_address`),
    KEY `idx_health_status` (`health_status`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MCP 服务实例表';

-- =====================================================
-- 3. API Key 认证表
-- =====================================================
DROP TABLE IF EXISTS `mcp_api_key`;
CREATE TABLE `mcp_api_key` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `api_key` VARCHAR(128) NOT NULL COMMENT 'API Key（加密存储）',
    `name` VARCHAR(128) NOT NULL COMMENT 'API Key 名称',
    `description` VARCHAR(512) COMMENT 'API Key 描述',
    `owner` VARCHAR(64) NOT NULL COMMENT '所有者（用户 ID 或系统标识）',
    `allowed_services` TEXT COMMENT '允许访问的服务列表（JSON 数组）',
    `rate_limit_config_id` BIGINT COMMENT '限流配置 ID',
    `expires_at` DATETIME COMMENT '过期时间',
    `last_used_at` DATETIME COMMENT '最后使用时间',
    `usage_count` BIGINT NOT NULL DEFAULT 0 COMMENT '使用次数统计',
    `status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE, INACTIVE, EXPIRED',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` VARCHAR(64) COMMENT '创建人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_api_key` (`api_key`),
    KEY `idx_owner` (`owner`),
    KEY `idx_status` (`status`),
    KEY `idx_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API Key 认证表';

-- =====================================================
-- 4. 请求日志表
-- =====================================================
DROP TABLE IF EXISTS `mcp_request_log`;
CREATE TABLE `mcp_request_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `request_id` VARCHAR(64) NOT NULL COMMENT '请求 ID（唯一标识）',
    `service_id` BIGINT COMMENT '服务 ID',
    `service_code` VARCHAR(64) COMMENT '服务编码',
    `instance_address` VARCHAR(256) COMMENT '服务实例地址',
    `api_key_id` BIGINT COMMENT 'API Key ID',
    `client_ip` VARCHAR(64) COMMENT '客户端 IP',
    `request_method` VARCHAR(16) COMMENT '请求方法',
    `request_path` VARCHAR(1024) COMMENT '请求路径',
    `request_headers` TEXT COMMENT '请求头（JSON 格式）',
    `request_body` TEXT COMMENT '请求体（JSON 格式）',
    `status_code` INT COMMENT '响应状态码',
    `response_headers` TEXT COMMENT '响应头（JSON 格式）',
    `response_body` TEXT COMMENT '响应体（JSON 格式）',
    `duration_ms` BIGINT COMMENT '处理时长（毫秒）',
    `success` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否成功',
    `error_message` VARCHAR(2048) COMMENT '错误信息',
    `mcp_tool_name` VARCHAR(128) COMMENT 'MCP 工具名称',
    `mcp_request_type` VARCHAR(32) COMMENT 'MCP 请求类型：TOOL, RESOURCE, PROMPT',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_request_id` (`request_id`),
    KEY `idx_service_id` (`service_id`),
    KEY `idx_service_code` (`service_code`),
    KEY `idx_api_key_id` (`api_key_id`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_status_code` (`status_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='请求日志表';

-- =====================================================
-- 5. 限流配置表
-- =====================================================
DROP TABLE IF EXISTS `mcp_rate_limit_config`;
CREATE TABLE `mcp_rate_limit_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `name` VARCHAR(128) NOT NULL COMMENT '配置名称',
    `description` VARCHAR(512) COMMENT '配置描述',
    `config_type` VARCHAR(32) NOT NULL COMMENT '配置类型：GLOBAL, SERVICE, API_KEY, IP',
    `target_id` VARCHAR(64) COMMENT '目标 ID（服务 ID、API Key ID 等）',
    `algorithm` VARCHAR(32) NOT NULL COMMENT '限流算法：FIXED_WINDOW, SLIDING_WINDOW, TOKEN_BUCKET, LEAKY_BUCKET',
    `limit` INT NOT NULL COMMENT '请求限制数量',
    `window_seconds` INT NOT NULL COMMENT '时间窗口（秒）',
    `bucket_capacity` INT COMMENT 'Token 桶容量（仅 TOKEN_BUCKET 算法）',
    `refill_rate` DOUBLE COMMENT 'Token 补充速率（个/秒）',
    `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    `reject_message` VARCHAR(256) COMMENT '限流响应消息',
    `reject_status_code` INT NOT NULL DEFAULT 429 COMMENT '限流响应 HTTP 状态码',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` VARCHAR(64) COMMENT '创建人',
    `updated_by` VARCHAR(64) COMMENT '更新人',
    PRIMARY KEY (`id`),
    KEY `idx_config_type` (`config_type`),
    KEY `idx_target_id` (`target_id`),
    KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='限流配置表';

-- =====================================================
-- 初始化数据
-- =====================================================

-- 插入默认限流配置
INSERT INTO `mcp_rate_limit_config` (`name`, `description`, `config_type`, `algorithm`, `limit`, `window_seconds`, `enabled`) 
VALUES 
    ('全局默认限流', '全局默认的限流配置', 'GLOBAL', 'SLIDING_WINDOW', 100, 60, 1),
    ('高频服务限流', '针对高频服务的限流配置', 'SERVICE', 'TOKEN_BUCKET', 1000, 60, 0);

-- 插入示例服务（可选）
INSERT INTO `mcp_service` (`service_code`, `service_name`, `description`, `version`, `service_type`, `base_url`, `status`, `mcp_enabled`) 
VALUES 
    ('demo-service', '示例服务', '这是一个示例服务', '1.0.0', 'HTTP', 'http://localhost:8081', 'REGISTERED', 1);
