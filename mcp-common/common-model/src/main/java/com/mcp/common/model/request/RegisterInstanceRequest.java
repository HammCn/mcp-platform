package com.mcp.common.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册服务实例请求
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterInstanceRequest {

    /**
     * 服务 ID
     */
    @NotBlank(message = "服务 ID 不能为空")
    private Long serviceId;

    /**
     * 实例地址（IP:Port）
     */
    @NotBlank(message = "实例地址不能为空")
    private String instanceAddress;

    /**
     * 实例元数据（JSON 格式）
     */
    private String metadata;

    /**
     * 权重（用于负载均衡）
     */
    @Min(value = 1, message = "权重最小值为 1")
    @Builder.Default
    private Integer weight = 100;
}
