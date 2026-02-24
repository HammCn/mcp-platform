package com.mcp.common.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建 API Key 请求
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateApiKeyRequest {

    /**
     * API Key 名称
     */
    @NotBlank(message = "API Key 名称不能为空")
    @Size(max = 128, message = "名称长度不能超过 128")
    private String name;

    /**
     * API Key 描述
     */
    @Size(max = 512, message = "描述长度不能超过 512")
    private String description;

    /**
     * 所有者
     */
    @NotBlank(message = "所有者不能为空")
    private String owner;

    /**
     * 允许访问的服务列表
     */
    private List<String> allowedServices;

    /**
     * 限流配置 ID
     */
    private Long rateLimitConfigId;

    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;
}
