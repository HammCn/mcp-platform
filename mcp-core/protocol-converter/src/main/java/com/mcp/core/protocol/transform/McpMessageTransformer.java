package com.mcp.core.protocol.transform;

import com.mcp.common.core.util.JsonUtil;
import com.mcp.common.model.dto.McpMessage;
import com.mcp.common.model.dto.McpTool;
import com.mcp.core.protocol.mcp.McpProtocol;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * MCP 消息转换器
 * 负责 MCP 协议消息的转换和处理
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Slf4j
@Component
public class McpMessageTransformer {

    /**
     * 转换 MCP Tool 到 MCP Protocol Tool
     */
    public McpProtocol.Tool convertToProtocolTool(McpTool tool) {
        return McpProtocol.Tool.builder()
                .name(tool.getName())
                .description(tool.getDescription())
                .inputSchema(convertInputSchema(tool.getInputSchema()))
                .build();
    }

    /**
     * 转换 InputSchema
     */
    public McpProtocol.InputSchema convertInputSchema(McpTool.InputSchema schema) {
        if (schema == null) {
            return null;
        }
        return McpProtocol.InputSchema.builder()
                .type(schema.getType())
                .properties(schema.getProperties())
                .required(schema.getRequired())
                .build();
    }

    /**
     * 创建 Initialize 响应
     */
    public McpMessage createInitializeResponse(Object id, String serverName, String serverVersion) {
        McpProtocol.InitializeResult result = McpProtocol.InitializeResult.builder()
                .protocolVersion("2024-11-05")
                .capabilities(McpProtocol.ServerCapabilities.builder()
                        .tools(McpProtocol.ToolsCapability.builder().listChanged(true).build())
                        .resources(McpProtocol.ResourcesCapability.builder()
                                .subscribe(true)
                                .listChanged(true)
                                .build())
                        .prompts(McpProtocol.PromptsCapability.builder().listChanged(true).build())
                        .logging(McpProtocol.LoggingCapability.builder().build())
                        .build())
                .serverInfo(McpProtocol.ServerInfo.builder()
                        .name(serverName)
                        .version(serverVersion)
                        .build())
                .build();

        return McpMessage.successResponse(id, result);
    }

    /**
     * 创建 Tools List 响应
     */
    public McpMessage createToolsListResponse(Object id, List<McpTool> tools) {
        List<McpProtocol.Tool> protocolTools = tools.stream()
                .map(this::convertToProtocolTool)
                .toList();

        McpProtocol.ToolsListResult result = McpProtocol.ToolsListResult.builder()
                .tools(protocolTools)
                .build();

        return McpMessage.successResponse(id, result);
    }

    /**
     * 创建 Tools Call 响应（成功）
     */
    public McpMessage createToolCallSuccessResponse(Object id, String resultText) {
        McpProtocol.Content content = McpProtocol.Content.builder()
                .type("text")
                .text(resultText)
                .build();

        McpProtocol.ToolCallResult result = McpProtocol.ToolCallResult.builder()
                .content(Collections.singletonList(content))
                .isError(false)
                .build();

        return McpMessage.successResponse(id, result);
    }

    /**
     * 创建 Tools Call 响应（错误）
     */
    public McpMessage createToolCallErrorResponse(Object id, String errorMessage) {
        McpProtocol.Content content = McpProtocol.Content.builder()
                .type("text")
                .text(errorMessage)
                .build();

        McpProtocol.ToolCallResult result = McpProtocol.ToolCallResult.builder()
                .content(Collections.singletonList(content))
                .isError(true)
                .build();

        return McpMessage.successResponse(id, result);
    }

    /**
     * 创建 Resources List 响应
     */
    public McpMessage createResourcesListResponse(Object id, List<McpProtocol.Resource> resources) {
        McpProtocol.ResourcesListResult result = McpProtocol.ResourcesListResult.builder()
                .resources(resources)
                .build();

        return McpMessage.successResponse(id, result);
    }

    /**
     * 创建 Resources Read 响应
     */
    public McpMessage createResourceReadResponse(Object id, String uri, String content, String mimeType) {
        McpProtocol.ResourceContents resourceContent = McpProtocol.ResourceContents.builder()
                .uri(uri)
                .mimeType(mimeType != null ? mimeType : "text/plain")
                .text(content)
                .build();

        McpProtocol.ResourceReadResult result = McpProtocol.ResourceReadResult.builder()
                .contents(Collections.singletonList(resourceContent))
                .build();

        return McpMessage.successResponse(id, result);
    }

    /**
     * 创建 Prompts List 响应
     */
    public McpMessage createPromptsListResponse(Object id, List<McpProtocol.Prompt> prompts) {
        McpProtocol.PromptsListResult result = McpProtocol.PromptsListResult.builder()
                .prompts(prompts)
                .build();

        return McpMessage.successResponse(id, result);
    }

    /**
     * 创建 Prompts Get 响应
     */
    public McpMessage createPromptGetResponse(Object id, String description, 
                                               List<McpProtocol.PromptMessage> messages) {
        McpProtocol.PromptGetResult result = McpProtocol.PromptGetResult.builder()
                .description(description)
                .messages(messages)
                .build();

        return McpMessage.successResponse(id, result);
    }

    /**
     * 创建错误响应
     */
    public McpMessage createErrorResponse(Object id, int code, String message) {
        return McpMessage.errorResponse(id, code, message);
    }

    /**
     * 解析 Tool Call 参数
     */
    public McpProtocol.ToolCallParams parseToolCallParams(Map<String, Object> params) {
        if (params == null) {
            return McpProtocol.ToolCallParams.builder().build();
        }

        String name = params.get("name") != null ? params.get("name").toString() : null;
        
        @SuppressWarnings("unchecked")
        Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");

        return McpProtocol.ToolCallParams.builder()
                .name(name)
                .arguments(arguments)
                .build();
    }

    /**
     * 将任意对象转换为 MCP 响应文本
     */
    public String convertToResponseText(Object result) {
        if (result == null) {
            return "null";
        }
        if (result instanceof String) {
            return (String) result;
        }
        return JsonUtil.toPrettyJson(result);
    }
}
