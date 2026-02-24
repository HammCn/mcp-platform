package com.mcp.common.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * MCP 协议请求/响应模型
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class McpMessage {

    /**
     * JSON-RPC 版本
     */
    private String jsonrpc = "2.0";

    /**
     * 消息 ID
     */
    private Object id;

    /**
     * 方法名
     */
    private String method;

    /**
     * 方法参数
     */
    private Map<String, Object> params;

    /**
     * 响应结果
     */
    private Object result;

    /**
     * 错误信息
     */
    private McpError error;

    /**
     * MCP 错误
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class McpError {
        private Integer code;
        private String message;
        private Object data;
    }

    /**
     * 创建请求消息
     */
    public static McpMessage request(Object id, String method, Map<String, Object> params) {
        return McpMessage.builder()
                .jsonrpc("2.0")
                .id(id)
                .method(method)
                .params(params)
                .build();
    }

    /**
     * 创建成功响应
     */
    public static McpMessage successResponse(Object id, Object result) {
        return McpMessage.builder()
                .jsonrpc("2.0")
                .id(id)
                .result(result)
                .build();
    }

    /**
     * 创建错误响应
     */
    public static McpMessage errorResponse(Object id, int code, String message) {
        return McpMessage.builder()
                .jsonrpc("2.0")
                .id(id)
                .error(new McpError(code, message, null))
                .build();
    }
}
