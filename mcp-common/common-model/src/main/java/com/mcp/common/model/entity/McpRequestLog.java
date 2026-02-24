package com.mcp.common.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 请求日志实体
 * 记录所有通过 MCP 网关的请求
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Entity
@Table(name = "mcp_request_log", indexes = {
    @Index(name = "idx_request_id", columnList = "request_id"),
    @Index(name = "idx_service_id", columnList = "service_id"),
    @Index(name = "idx_api_key_id", columnList = "api_key_id"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_status_code", columnList = "status_code")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class McpRequestLog {

    /**
     * 主键 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 请求 ID（唯一标识）
     */
    @Column(name = "request_id", length = 64, nullable = false)
    private String requestId;

    /**
     * 服务 ID
     */
    @Column(name = "service_id")
    private Long serviceId;

    /**
     * 服务编码
     */
    @Column(name = "service_code", length = 64)
    private String serviceCode;

    /**
     * 服务实例地址
     */
    @Column(name = "instance_address", length = 256)
    private String instanceAddress;

    /**
     * API Key ID
     */
    @Column(name = "api_key_id")
    private Long apiKeyId;

    /**
     * 客户端 IP
     */
    @Column(name = "client_ip", length = 64)
    private String clientIp;

    /**
     * 请求方法
     */
    @Column(name = "request_method", length = 16)
    private String requestMethod;

    /**
     * 请求路径
     */
    @Column(name = "request_path", length = 1024)
    private String requestPath;

    /**
     * 请求头（JSON 格式）
     */
    @Column(name = "request_headers", columnDefinition = "TEXT")
    private String requestHeaders;

    /**
     * 请求体（JSON 格式，可能截断）
     */
    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;

    /**
     * 响应状态码
     */
    @Column(name = "status_code")
    private Integer statusCode;

    /**
     * 响应头（JSON 格式）
     */
    @Column(name = "response_headers", columnDefinition = "TEXT")
    private String responseHeaders;

    /**
     * 响应体（JSON 格式，可能截断）
     */
    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    /**
     * 处理时长（毫秒）
     */
    @Column(name = "duration_ms")
    private Long durationMs;

    /**
     * 是否成功
     */
    @Column(name = "success", nullable = false)
    @Builder.Default
    private Boolean success = true;

    /**
     * 错误信息
     */
    @Column(name = "error_message", length = 2048)
    private String errorMessage;

    /**
     * MCP 工具名称（如果是 MCP 请求）
     */
    @Column(name = "mcp_tool_name", length = 128)
    private String mcpToolName;

    /**
     * MCP 请求类型：TOOL, RESOURCE, PROMPT
     */
    @Column(name = "mcp_request_type", length = 32)
    private String mcpRequestType;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
