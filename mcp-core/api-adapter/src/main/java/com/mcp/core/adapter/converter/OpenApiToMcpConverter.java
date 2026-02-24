package com.mcp.core.adapter.converter;

import com.mcp.common.core.util.JsonUtil;
import com.mcp.common.model.dto.McpTool;
import com.mcp.core.adapter.openapi.OpenApiParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * OpenAPI 到 MCP Tool 转换器
 * 将 OpenAPI 路径转换为 MCP Tool 定义
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenApiToMcpConverter {

    private final OpenApiParser openApiParser;

    /**
     * 将 OpenAPI 规范转换为 MCP Tool 列表
     *
     * @param openApiSpec OpenAPI 规范内容或 URL
     * @param isUrl 是否为 URL
     * @param toolPrefix 工具名称前缀
     * @return MCP Tool 列表
     */
    public List<McpTool> convert(String openApiSpec, boolean isUrl, String toolPrefix) {
        io.swagger.v3.oas.models.OpenAPI openAPI = openApiParser.parse(openApiSpec, isUrl);
        List<OpenApiParser.ApiPathInfo> paths = openApiParser.extractPaths(openAPI);
        
        return paths.stream()
                .map(path -> convertToMcpTool(path, toolPrefix))
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 将单个 API 路径转换为 MCP Tool
     */
    public McpTool convertToMcpTool(OpenApiParser.ApiPathInfo pathInfo, String toolPrefix) {
        // 生成工具名称
        String toolName = generateToolName(pathInfo, toolPrefix);
        
        // 生成工具描述
        String description = generateDescription(pathInfo);
        
        // 生成输入 Schema
        McpTool.InputSchema inputSchema = generateInputSchema(pathInfo);
        
        return McpTool.builder()
                .name(toolName)
                .description(description)
                .inputSchema(inputSchema)
                .build();
    }

    /**
     * 生成工具名称
     */
    private String generateToolName(OpenApiParser.ApiPathInfo pathInfo, String prefix) {
        // 优先使用 operationId
        if (pathInfo.getOperationId() != null && !pathInfo.getOperationId().isEmpty()) {
            String operationId = pathInfo.getOperationId();
            // 转换为 snake_case 格式
            operationId = operationId.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
            return prefix != null ? prefix + "_" + operationId : operationId;
        }
        
        // 使用 method + path 生成
        String method = pathInfo.getMethod().toLowerCase();
        String path = pathInfo.getPath()
                .replaceAll("\\{[^}]+\\}", "")  // 移除路径参数
                .replaceAll("/", "_")
                .replaceAll("^_", "")
                .replaceAll("_$", "")
                .replaceAll("_+", "_");
        
        if (path.isEmpty()) {
            path = "root";
        }
        
        String name = method + "_" + path;
        return prefix != null ? prefix + "_" + name : name;
    }

    /**
     * 生成工具描述
     */
    private String generateDescription(OpenApiParser.ApiPathInfo pathInfo) {
        StringBuilder desc = new StringBuilder();
        
        // 使用 summary 或 description
        if (pathInfo.getSummary() != null && !pathInfo.getSummary().isEmpty()) {
            desc.append(pathInfo.getSummary());
        } else if (pathInfo.getDescription() != null && !pathInfo.getDescription().isEmpty()) {
            desc.append(pathInfo.getDescription());
        } else {
            desc.append(String.format("%s %s", pathInfo.getMethod(), pathInfo.getPath()));
        }
        
        // 添加标签信息
        if (pathInfo.getTags() != null && !pathInfo.getTags().isEmpty()) {
            desc.append(" [Tags: ").append(String.join(", ", pathInfo.getTags())).append("]");
        }
        
        return desc.toString();
    }

    /**
     * 生成输入 Schema
     */
    private McpTool.InputSchema generateInputSchema(OpenApiParser.ApiPathInfo pathInfo) {
        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();
        
        // 添加路径参数
        if (pathInfo.getParameters() != null) {
            for (OpenApiParser.ParameterInfo param : pathInfo.getParameters()) {
                String paramName = param.getName();
                Map<String, Object> paramSchema = parseParameterSchema(param);
                properties.put(paramName, paramSchema);
                
                if (Boolean.TRUE.equals(param.getRequired())) {
                    required.add(paramName);
                }
            }
        }
        
        // 添加请求体参数
        if (pathInfo.getRequestBody() != null && 
            pathInfo.getRequestBody().getContent() != null) {
            
            for (Map.Entry<String, OpenApiParser.ContentInfo> entry : 
                    pathInfo.getRequestBody().getContent().entrySet()) {
                
                if ("application/json".equals(entry.getKey()) && 
                    entry.getValue().getSchema() != null) {
                    
                    Map<String, Object> bodySchema = JsonUtil.fromJson(
                            entry.getValue().getSchema(), Map.class);
                    
                    if (bodySchema != null && bodySchema.get("properties") != null) {
                        Map<String, Object> bodyProps = 
                                (Map<String, Object>) bodySchema.get("properties");
                        
                        // 添加 body_ 前缀避免与路径参数冲突
                        for (Map.Entry<String, Object> prop : bodyProps.entrySet()) {
                            properties.put("body_" + prop.getKey(), prop.getValue());
                        }
                        
                        // 添加必填字段
                        if (bodySchema.get("required") != null) {
                            List<String> bodyRequired = 
                                    (List<String>) bodySchema.get("required");
                            for (String req : bodyRequired) {
                                required.add("body_" + req);
                            }
                        }
                    }
                }
            }
        }
        
        // 如果没有参数，添加一个空的 body 参数
        if (properties.isEmpty()) {
            Map<String, Object> emptyBody = new LinkedHashMap<>();
            emptyBody.put("type", "object");
            emptyBody.put("description", "请求体（可选）");
            properties.put("body", emptyBody);
        }
        
        return McpTool.InputSchema.builder()
                .type("object")
                .properties(properties)
                .required(required.isEmpty() ? null : required)
                .build();
    }

    /**
     * 解析参数 Schema
     */
    private Map<String, Object> parseParameterSchema(OpenApiParser.ParameterInfo param) {
        Map<String, Object> schema = new LinkedHashMap<>();
        
        if (param.getSchema() != null) {
            Map<String, Object> paramSchema = JsonUtil.fromJson(param.getSchema(), Map.class);
            if (paramSchema != null) {
                schema.put("type", paramSchema.getOrDefault("type", "string"));
                if (paramSchema.get("description") != null) {
                    schema.put("description", paramSchema.get("description"));
                }
                if (paramSchema.get("enum") != null) {
                    schema.put("enum", paramSchema.get("enum"));
                }
                if (paramSchema.get("format") != null) {
                    schema.put("format", paramSchema.get("format"));
                }
            }
        } else {
            schema.put("type", "string");
        }
        
        if (param.getDescription() != null) {
            schema.put("description", param.getDescription());
        }
        
        return schema;
    }

    /**
     * 从工具名称生成操作 ID
     */
    public String extractOperationIdFromToolName(String toolName) {
        // 移除前缀
        String operationId = toolName;
        if (toolName.contains("_")) {
            String[] parts = toolName.split("_", 2);
            if (parts.length > 1) {
                operationId = parts[1];
            }
        }
        
        // 转换为 camelCase
        String[] words = operationId.split("_");
        StringBuilder result = new StringBuilder(words[0].toLowerCase());
        for (int i = 1; i < words.length; i++) {
            result.append(Character.toUpperCase(words[i].charAt(0)))
                  .append(words[i].substring(1).toLowerCase());
        }
        
        return result.toString();
    }
}
