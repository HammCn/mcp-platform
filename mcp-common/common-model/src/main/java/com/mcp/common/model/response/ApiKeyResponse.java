package com.mcp.common.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API Key 信息响应
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyResponse {

    /**
     * API Key ID
     */
    private Long id;

    /**
     * API Key（仅创建时返回）
     */
    private String apiKey;

    /**
     * API Key 名称
     */
    private String name;

    /**
     * API Key 描述
     */
    private String description;

    /**
     * 所有者
     */
    private String owner;

    /**
     * 允许访问的服务列表
     */
    private String allowedServices;

    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;

    /**
     * 最后使用时间
     */
    private LocalDateTime lastUsedAt;

    /**
     * 使用次数
     */
    private Long usageCount;

    /**
     * 状态
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
