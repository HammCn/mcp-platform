package com.mcp.core.adapter.openapi;

import com.mcp.common.core.exception.McpException;
import com.mcp.common.core.util.JsonUtil;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * OpenAPI 规范解析器
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Slf4j
@Component
public class OpenApiParser {

    /**
     * 解析 OpenAPI 规范
     *
     * @param spec OpenAPI 规范内容或 URL
     * @param isUrl 是否为 URL
     * @return OpenAPI 对象
     */
    public OpenAPI parse(String spec, boolean isUrl) {
        if (spec == null || spec.trim().isEmpty()) {
            throw new McpException("OPENAPI_PARSE_ERROR", "OpenAPI 规范不能为空", 400);
        }

        SwaggerParseResult result;
        if (isUrl) {
            result = parseFromUrl(spec);
        } else {
            result = parseFromContent(spec);
        }

        if (result == null || result.getOpenAPI() == null) {
            String messages = result != null ? String.join(", ", result.getMessages()) : "unknown error";
            throw new McpException("OPENAPI_PARSE_ERROR", 
                    "解析 OpenAPI 规范失败：" + messages, 400);
        }

        return result.getOpenAPI();
    }

    /**
     * 从 URL 解析 OpenAPI 规范
     */
    private SwaggerParseResult parseFromUrl(String url) {
        log.info("从 URL 解析 OpenAPI 规范：{}", url);
        try {
            // 验证 URL 格式
            new URL(url);
        } catch (MalformedURLException e) {
            throw new McpException("INVALID_URL", "无效的 URL: " + url, 400);
        }

        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        options.setResolveFully(true);
        
        SwaggerParseResult result = new OpenAPIV3Parser().readLocation(url, null, options);
        
        if (result.getMessages() != null && !result.getMessages().isEmpty()) {
            log.warn("解析 OpenAPI URL 时出现警告：{}", result.getMessages());
        }
        
        return result;
    }

    /**
     * 从内容解析 OpenAPI 规范
     */
    private SwaggerParseResult parseFromContent(String content) {
        log.info("从内容解析 OpenAPI 规范");
        
        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        options.setResolveFully(true);
        
        SwaggerParseResult result = new OpenAPIV3Parser().readContents(content, null, options);
        
        if (result.getMessages() != null && !result.getMessages().isEmpty()) {
            log.warn("解析 OpenAPI 内容时出现警告：{}", result.getMessages());
        }
        
        return result;
    }

    /**
     * 提取所有 API 路径
     */
    public List<ApiPathInfo> extractPaths(OpenAPI openAPI) {
        List<ApiPathInfo> paths = new ArrayList<>();
        
        if (openAPI.getPaths() == null) {
            return paths;
        }

        String baseUrl = extractBaseUrl(openAPI);

        for (Map.Entry<String, PathItem> entry : openAPI.getPaths().entrySet()) {
            String path = entry.getKey();
            PathItem pathItem = entry.getValue();

            // 处理每种 HTTP 方法
            processOperation(path, pathItem.getGet(), "GET", baseUrl, paths);
            processOperation(path, pathItem.getPost(), "POST", baseUrl, paths);
            processOperation(path, pathItem.getPut(), "PUT", baseUrl, paths);
            processOperation(path, pathItem.getDelete(), "DELETE", baseUrl, paths);
            processOperation(path, pathItem.getPatch(), "PATCH", baseUrl, paths);
            processOperation(path, pathItem.getHead(), "HEAD", baseUrl, paths);
            processOperation(path, pathItem.getOptions(), "OPTIONS", baseUrl, paths);
        }

        return paths;
    }

    /**
     * 处理单个 Operation
     */
    private void processOperation(String path, Operation operation, String method, 
                                   String baseUrl, List<ApiPathInfo> paths) {
        if (operation == null) {
            return;
        }

        // 跳过标记为 x-mcp-ignore 的操作
        if (operation.getExtensions() != null && 
            Boolean.TRUE.equals(operation.getExtensions().get("x-mcp-ignore"))) {
            return;
        }

        ApiPathInfo info = ApiPathInfo.builder()
                .path(path)
                .method(method)
                .fullUrl(baseUrl + path)
                .operationId(operation.getOperationId())
                .summary(operation.getSummary())
                .description(operation.getDescription())
                .tags(operation.getTags())
                .parameters(extractParameters(operation.getParameters()))
                .requestBody(extractRequestBody(operation.getRequestBody()))
                .responses(extractResponses(operation.getResponses()))
                .build();

        paths.add(info);
    }

    /**
     * 提取基础 URL
     */
    public String extractBaseUrl(OpenAPI openAPI) {
        if (openAPI.getServers() != null && !openAPI.getServers().isEmpty()) {
            Server server = openAPI.getServers().get(0);
            return server.getUrl();
        }
        return "";
    }

    /**
     * 提取参数信息
     */
    private List<ParameterInfo> extractParameters(List<Parameter> parameters) {
        if (parameters == null) {
            return Collections.emptyList();
        }

        return parameters.stream()
                .map(param -> ParameterInfo.builder()
                        .name(param.getName())
                        .in(param.getIn())
                        .description(param.getDescription())
                        .required(Boolean.TRUE.equals(param.getRequired()))
                        .schema(param.getSchema() != null ? 
                                JsonUtil.toJson(param.getSchema()) : null)
                        .build())
                .toList();
    }

    /**
     * 提取请求体信息
     */
    private RequestBodyInfo extractRequestBody(io.swagger.v3.oas.models.parameters.RequestBody requestBody) {
        if (requestBody == null || requestBody.getContent() == null) {
            return null;
        }

        return RequestBodyInfo.builder()
                .description(requestBody.getDescription())
                .required(Boolean.TRUE.equals(requestBody.getRequired()))
                .content(extractContent(requestBody.getContent()))
                .build();
    }

    /**
     * 提取响应信息
     */
    private Map<String, ResponseInfo> extractResponses(
            io.swagger.v3.oas.models.responses.ApiResponses responses) {
        if (responses == null) {
            return Collections.emptyMap();
        }

        Map<String, ResponseInfo> result = new LinkedHashMap<>();
        for (Map.Entry<String, ApiResponse> entry : responses.entrySet()) {
            ApiResponse response = entry.getValue();
            result.put(entry.getKey(), ResponseInfo.builder()
                    .description(response.getDescription())
                    .content(response.getContent() != null ? 
                            extractContent(response.getContent()) : null)
                    .build());
        }
        return result;
    }

    /**
     * 提取内容信息
     */
    private Map<String, ContentInfo> extractContent(
            io.swagger.v3.oas.models.media.Content content) {
        if (content == null) {
            return Collections.emptyMap();
        }

        Map<String, ContentInfo> result = new LinkedHashMap<>();
        for (Map.Entry<String, io.swagger.v3.oas.models.media.MediaType> entry : 
                content.entrySet()) {
            io.swagger.v3.oas.models.media.MediaType mediaType = entry.getValue();
            result.put(entry.getKey(), ContentInfo.builder()
                    .schema(mediaType.getSchema() != null ? 
                            schemaToJson(mediaType.getSchema()) : null)
                    .example(mediaType.getExample())
                    .build());
        }
        return result;
    }

    /**
     * 将 Schema 转换为 JSON
     */
    private String schemaToJson(Schema<?> schema) {
        Map<String, Object> schemaMap = new LinkedHashMap<>();
        schemaMap.put("type", schema.getType());
        if (schema.getDescription() != null) {
            schemaMap.put("description", schema.getDescription());
        }
        if (schema.getProperties() != null) {
            Map<String, Object> properties = new LinkedHashMap<>();
            for (Map.Entry<String, Schema> entry : schema.getProperties().entrySet()) {
                properties.put(entry.getKey(), schemaToJson(entry.getValue()));
            }
            schemaMap.put("properties", properties);
        }
        if (schema.getRequired() != null) {
            schemaMap.put("required", schema.getRequired());
        }
        return JsonUtil.toJson(schemaMap);
    }

    /**
     * API 路径信息
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ApiPathInfo {
        private String path;
        private String method;
        private String fullUrl;
        private String operationId;
        private String summary;
        private String description;
        private List<String> tags;
        private List<ParameterInfo> parameters;
        private RequestBodyInfo requestBody;
        private Map<String, ResponseInfo> responses;
    }

    /**
     * 参数信息
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ParameterInfo {
        private String name;
        private String in;
        private String description;
        private Boolean required;
        private String schema;
    }

    /**
     * 请求体信息
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RequestBodyInfo {
        private String description;
        private Boolean required;
        private Map<String, ContentInfo> content;
    }

    /**
     * 响应信息
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ResponseInfo {
        private String description;
        private Map<String, ContentInfo> content;
    }

    /**
     * 内容信息
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ContentInfo {
        private String schema;
        private Object example;
    }
}
