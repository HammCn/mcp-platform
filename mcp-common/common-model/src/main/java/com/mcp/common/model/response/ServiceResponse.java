package com.mcp.common.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 服务信息响应
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponse {

    /**
     * 服务 ID
     */
    private Long id;

    /**
     * 服务编码
     */
    private String serviceCode;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务描述
     */
    private String description;

    /**
     * 服务版本
     */
    private String version;

    /**
     * 服务类型
     */
    private String serviceType;

    /**
     * 基础 URL
     */
    private String baseUrl;

    /**
     * 认证类型
     */
    private String authType;

    /**
     * 服务状态
     */
    private String status;

    /**
     * 是否启用 MCP
     */
    private Boolean mcpEnabled;

    /**
     * MCP 工具前缀
     */
    private String mcpToolPrefix;

    /**
     * 健康实例数量
     */
    private Integer healthyInstanceCount;

    /**
     * 总实例数量
     */
    private Integer totalInstanceCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
