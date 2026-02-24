package com.mcp.common.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建服务请求
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateServiceRequest {

    /**
     * 服务编码
     */
    @NotBlank(message = "服务编码不能为空")
    @Size(max = 64, message = "服务编码长度不能超过 64")
    private String serviceCode;

    /**
     * 服务名称
     */
    @NotBlank(message = "服务名称不能为空")
    @Size(max = 128, message = "服务名称长度不能超过 128")
    private String serviceName;

    /**
     * 服务描述
     */
    @Size(max = 1024, message = "服务描述长度不能超过 1024")
    private String description;

    /**
     * 服务版本
     */
    private String version;

    /**
     * 服务类型：HTTP, GRPC, WEBSOCKET
     */
    @NotBlank(message = "服务类型不能为空")
    private String serviceType;

    /**
     * 基础 URL
     */
    @Size(max = 512, message = "基础 URL 长度不能超过 512")
    private String baseUrl;

    /**
     * OpenAPI 规范 URL 或内容
     */
    private String openapiSpec;

    /**
     * OpenAPI 规范类型：URL, CONTENT
     */
    private String specType;

    /**
     * 认证类型：API_KEY, JWT, OAUTH2, NONE
     */
    private String authType;

    /**
     * 认证配置（JSON 格式）
     */
    private String authConfig;

    /**
     * 健康检查路径
     */
    private String healthCheckPath;

    /**
     * 是否启用 MCP 协议转换
     */
    @Builder.Default
    private Boolean mcpEnabled = true;

    /**
     * MCP 工具前缀
     */
    private String mcpToolPrefix;

    /**
     * 超时时间（毫秒）
     */
    private Integer timeoutMs;

    /**
     * 重试次数
     */
    private Integer retryCount;
}
