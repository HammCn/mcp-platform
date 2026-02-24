package com.mcp.common.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * MCP 服务定义实体
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Entity
@Table(name = "mcp_service", indexes = {
    @Index(name = "idx_service_code", columnList = "service_code"),
    @Index(name = "idx_service_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class McpService {

    /**
     * 主键 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 服务编码（唯一标识）
     */
    @Column(name = "service_code", length = 64, nullable = false, unique = true)
    private String serviceCode;

    /**
     * 服务名称
     */
    @Column(name = "service_name", length = 128, nullable = false)
    private String serviceName;

    /**
     * 服务描述
     */
    @Column(name = "description", length = 1024)
    private String description;

    /**
     * 服务版本
     */
    @Column(name = "version", length = 32)
    private String version;

    /**
     * 服务类型：HTTP, GRPC, WEBSOCKET
     */
    @Column(name = "service_type", length = 32, nullable = false)
    private String serviceType;

    /**
     * 基础 URL
     */
    @Column(name = "base_url", length = 512)
    private String baseUrl;

    /**
     * OpenAPI 规范 URL 或内容
     */
    @Column(name = "openapi_spec", columnDefinition = "TEXT")
    private String openapiSpec;

    /**
     * OpenAPI 规范类型：URL, CONTENT
     */
    @Column(name = "spec_type", length = 32)
    private String specType;

    /**
     * 认证类型：API_KEY, JWT, OAUTH2, NONE
     */
    @Column(name = "auth_type", length = 32)
    private String authType;

    /**
     * 认证配置（JSON 格式）
     */
    @Column(name = "auth_config", columnDefinition = "TEXT")
    private String authConfig;

    /**
     * 健康检查路径
     */
    @Column(name = "health_check_path", length = 256)
    private String healthCheckPath;

    /**
     * 服务状态：REGISTERED, HEALTHY, UNHEALTHY, DISABLED
     */
    @Column(name = "status", length = 32, nullable = false)
    private String status;

    /**
     * 是否启用 MCP 协议转换
     */
    @Column(name = "mcp_enabled", nullable = false)
    @Builder.Default
    private Boolean mcpEnabled = true;

    /**
     * MCP 工具前缀
     */
    @Column(name = "mcp_tool_prefix", length = 64)
    private String mcpToolPrefix;

    /**
     * 超时时间（毫秒）
     */
    @Column(name = "timeout_ms")
    private Integer timeoutMs;

    /**
     * 重试次数
     */
    @Column(name = "retry_count")
    private Integer retryCount;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 创建人
     */
    @Column(name = "created_by", length = 64)
    private String createdBy;

    /**
     * 更新人
     */
    @Column(name = "updated_by", length = 64)
    private String updatedBy;
}
