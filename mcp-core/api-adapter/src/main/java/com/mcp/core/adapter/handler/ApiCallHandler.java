package com.mcp.core.adapter.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcp.common.core.exception.McpException;
import com.mcp.common.core.exception.ServiceNotFoundException;
import com.mcp.common.core.util.JsonUtil;
import com.mcp.common.model.entity.McpService;
import com.mcp.common.model.entity.McpServiceInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API 调用处理器
 * 负责将 MCP Tool 调用转换为实际的 HTTP 请求
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiCallHandler {

    private final ObjectMapper objectMapper;
    
    /**
     * WebClient 缓存（按服务 ID）
     */
    private final Map<Long, WebClient> webClientCache = new ConcurrentHashMap<>();

    /**
     * 调用 API
     *
     * @param service 服务定义
     * @param instance 服务实例
     * @param method HTTP 方法
     * @param path 请求路径
     * @param parameters 请求参数
     * @param headers 请求头
     * @return 响应体
     */
    public Mono<String> callApi(McpService service, McpServiceInstance instance,
                                 String method, String path,
                                 Map<String, Object> parameters,
                                 Map<String, String> headers) {
        
        log.info("调用 API: service={}, method={}, path={}", 
                service.getServiceCode(), method, path);
        
        WebClient webClient = getOrCreateWebClient(service, instance);

        // 构建 URI
        String finalUri = path;
        if (!shouldHaveBody(method.toUpperCase()) && parameters != null && !parameters.isEmpty()) {
            // 为 GET/HEAD 等请求添加查询参数
            StringBuilder uriBuilder = new StringBuilder(path);
            boolean hasQuery = false;
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                if (entry.getValue() != null) {
                    uriBuilder.append(hasQuery ? "&" : "?")
                              .append(entry.getKey())
                              .append("=")
                              .append(entry.getValue().toString());
                    hasQuery = true;
                }
            }
            finalUri = uriBuilder.toString();
        }

        // 构建请求
        WebClient.RequestBodySpec requestSpec = webClient
                .method(org.springframework.http.HttpMethod.valueOf(method.toUpperCase()))
                .uri(finalUri);

        // 添加请求头
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        if (headers != null) {
            headers.forEach(httpHeaders::set);
        }
        requestSpec.headers(h -> h.addAll(httpHeaders));

        // 处理请求体
        Mono<String> responseMono;
        if (shouldHaveBody(method.toUpperCase()) && parameters != null && !parameters.isEmpty()) {
            // 分离路径参数和请求体参数
            Map<String, Object> bodyParams = extractBodyParams(parameters);
            if (!bodyParams.isEmpty()) {
                responseMono = requestSpec
                        .bodyValue(bodyParams)
                        .retrieve()
                        .bodyToMono(String.class);
            } else {
                responseMono = requestSpec
                        .retrieve()
                        .bodyToMono(String.class);
            }
        } else {
            responseMono = requestSpec
                    .retrieve()
                    .bodyToMono(String.class);
        }
        
        // 处理超时
        int timeoutMs = service.getTimeoutMs() != null ? service.getTimeoutMs() : 30000;
        responseMono = responseMono.timeout(Duration.ofMillis(timeoutMs));
        
        // 处理错误
        return responseMono
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.error("API 调用失败：status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
                    return Mono.error(new McpException("API_ERROR", 
                            "API 调用失败：" + e.getStatusCode() + " - " + e.getResponseBodyAsString(),
                            e.getStatusCode().value()));
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("API 调用异常：", e);
                    return Mono.error(new McpException("API_ERROR", 
                            "API 调用异常：" + e.getMessage()));
                });
    }

    /**
     * 获取或创建 WebClient
     */
    private WebClient getOrCreateWebClient(McpService service, McpServiceInstance instance) {
        return webClientCache.computeIfAbsent(service.getId(), key -> {
            String baseUrl = instance.getInstanceAddress();
            if (!baseUrl.startsWith("http")) {
                baseUrl = service.getBaseUrl();
                if (baseUrl != null && !baseUrl.isEmpty() && !baseUrl.endsWith("/")) {
                    baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl;
                }
            }
            
            log.debug("创建 WebClient: baseUrl={}", baseUrl);
            
            return WebClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .build();
        });
    }

    /**
     * 判断方法是否应该有请求体
     */
    private boolean shouldHaveBody(String method) {
        return "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method);
    }

    /**
     * 提取请求体参数（body_ 前缀的参数）
     */
    private Map<String, Object> extractBodyParams(Map<String, Object> parameters) {
        return parameters.entrySet().stream()
                .filter(e -> e.getKey().startsWith("body_"))
                .collect(java.util.stream.Collectors.toMap(
                        e -> e.getKey().substring(5),  // 移除 body_ 前缀
                        Map.Entry::getValue
                ));
    }

    /**
     * 替换路径参数
     */
    public String replacePathParameters(String path, Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return path;
        }
        
        String result = path;
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            if (result.contains(placeholder)) {
                result = result.replace(placeholder, String.valueOf(entry.getValue()));
            }
        }
        
        // 移除已替换的参数
        parameters.entrySet().removeIf(e -> 
            path.contains("{" + e.getKey() + "}"));
        
        return result;
    }

    /**
     * 解析 JSON 响应
     */
    public JsonNode parseJsonResponse(String responseBody) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return objectMapper.createObjectNode();
        }
        
        try {
            return objectMapper.readTree(responseBody);
        } catch (Exception e) {
            log.warn("解析 JSON 响应失败，返回原始文本：{}", responseBody);
            // 返回包含原始文本的节点
            return objectMapper.createObjectNode()
                    .put("raw_response", responseBody);
        }
    }

    /**
     * 清除 WebClient 缓存
     */
    public void clearWebClientCache(Long serviceId) {
        if (serviceId != null) {
            webClientCache.remove(serviceId);
        } else {
            webClientCache.clear();
        }
    }
}
