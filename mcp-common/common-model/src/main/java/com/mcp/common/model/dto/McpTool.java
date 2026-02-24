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
 * MCP Tool 相关模型
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
public class McpTool {

    /**
     * Tool 名称
     */
    private String name;

    /**
     * Tool 描述
     */
    private String description;

    /**
     * 输入 Schema（JSON Schema）
     */
    private InputSchema inputSchema;

    /**
     * 输入 Schema
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InputSchema {
        private String type;
        private Map<String, Object> properties;
        private List<String> required;
        private Map<String, Object> additionalProperties;
    }

    /**
     * Tool 调用结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CallResult {
        private List<Content> content;
        private Boolean isError;
    }

    /**
     * 内容类型
     */
    public enum ContentType {
        TEXT, IMAGE, RESOURCE
    }

    /**
     * 内容
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        private ContentType type;
        private String text;
        private String data;
        private String mimeType;
    }
}
