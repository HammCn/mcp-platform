package com.mcp.core.protocol.mcp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * MCP 协议模型定义
 *
 * @author MCP Platform
 * @since 1.0.0
 */
public class McpProtocol {

    /**
     * MCP 服务器信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ServerInfo {
        private String name;
        private String version;
    }

    /**
     * MCP 客户端能力
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ClientCapabilities {
        private RootsCapability roots;
        private SamplingCapability sampling;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RootsCapability {
        private Boolean listChanged;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SamplingCapability {
        // 空对象，表示支持
    }

    /**
     * MCP 服务器能力
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ServerCapabilities {
        private ToolsCapability tools;
        private ResourcesCapability resources;
        private PromptsCapability prompts;
        private LoggingCapability logging;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ToolsCapability {
        private Boolean listChanged;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ResourcesCapability {
        private Boolean subscribe;
        private Boolean listChanged;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PromptsCapability {
        private Boolean listChanged;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LoggingCapability {
        // 空对象，表示支持
    }

    /**
     * Initialize 请求参数
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InitializeParams {
        private String protocolVersion;
        private ClientCapabilities capabilities;
        private ClientInfo clientInfo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ClientInfo {
        private String name;
        private String version;
    }

    /**
     * Initialize 响应结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InitializeResult {
        private String protocolVersion;
        private ServerCapabilities capabilities;
        private ServerInfo serverInfo;
        private String instructions;
    }

    /**
     * Tools List 响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ToolsListResult {
        private List<Tool> tools;
    }

    /**
     * Tool 定义
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Tool {
        private String name;
        private String description;
        private InputSchema inputSchema;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InputSchema {
        private String type;
        private Map<String, Object> properties;
        private List<String> required;
    }

    /**
     * Tools Call 请求参数
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ToolCallParams {
        private String name;
        private Map<String, Object> arguments;
    }

    /**
     * Tools Call 响应结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ToolCallResult {
        private List<Content> content;
        private Boolean isError;
    }

    /**
     * Resources List 响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResourcesListResult {
        private List<Resource> resources;
    }

    /**
     * Resource 定义
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Resource {
        private String uri;
        private String name;
        private String description;
        private String mimeType;
    }

    /**
     * Resources Read 请求参数
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResourceReadParams {
        private String uri;
    }

    /**
     * Resources Read 响应结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResourceReadResult {
        private List<ResourceContents> contents;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ResourceContents {
        private String uri;
        private String mimeType;
        private String text;
        private String blob;
    }

    /**
     * Prompts List 响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PromptsListResult {
        private List<Prompt> prompts;
    }

    /**
     * Prompt 定义
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Prompt {
        private String name;
        private String description;
        private List<PromptArgument> arguments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PromptArgument {
        private String name;
        private String description;
        private Boolean required;
    }

    /**
     * Prompts Get 请求参数
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PromptGetParams {
        private String name;
        private Map<String, String> arguments;
    }

    /**
     * Prompts Get 响应结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PromptGetResult {
        private String description;
        private List<PromptMessage> messages;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PromptMessage {
        private String role;
        private Content content;
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Content {
        private String type;
        private String text;
        private String data;
        private String mimeType;
    }
}
