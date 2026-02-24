package com.mcp.gateway.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.mcp.common.core.util.IdUtil;
import com.mcp.common.core.util.JsonUtil;
import com.mcp.common.model.dto.McpMessage;
import com.mcp.gateway.server.McpServerHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP SSE (Server-Sent Events) 端点
 * 支持 MCP 协议的 SSE 传输
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/mcp")
@RequiredArgsConstructor
public class McpSseHandler {

    private final McpServerHandler serverHandler;

    /**
     * SSE 会话管理
     * sessionId -> Sinks.Many<SseEvent>
     */
    private final Map<String, Sinks.Many<SseEvent>> sessionSinks = new ConcurrentHashMap<>();

    /**
     * 会话到端点的映射
     */
    private final Map<String, String> sessionEndpoints = new ConcurrentHashMap<>();

    /**
     * SSE 连接端点
     * 客户端首先连接此端点建立 SSE 连接
     */
    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<SseEvent> sseConnection(
            @RequestParam(required = false) String sessionId) {
        
        String actualSessionId = sessionId != null ? sessionId : IdUtil.uuid();
        
        log.info("新建 SSE 连接：sessionId={}", actualSessionId);
        
        Sinks.Many<SseEvent> sink = Sinks.many().unicast().onBackpressureBuffer();
        sessionSinks.put(actualSessionId, sink);
        
        // 发送 endpoint 事件，告知客户端 POST 端点
        SseEvent endpointEvent = SseEvent.builder()
                .event("endpoint")
                .data("/mcp/message?sessionId=" + actualSessionId)
                .build();
        sink.tryEmitNext(endpointEvent);
        
        // 心跳
        Flux<SseEvent> heartbeat = Flux.interval(Duration.ofSeconds(30))
                .map(tick -> SseEvent.builder().event("ping").data("{}").build());
        
        return Flux.merge(sink.asFlux(), heartbeat)
                .doOnCancel(() -> {
                    log.info("SSE 连接关闭：sessionId={}", actualSessionId);
                    sessionSinks.remove(actualSessionId);
                    sessionEndpoints.remove(actualSessionId);
                });
    }

    /**
     * MCP 消息处理端点
     * 客户端通过 POST 发送 MCP 消息
     */
    @PostMapping("/message")
    @ResponseBody
    public Mono<Map<String, Object>> handleMessage(
            @RequestParam String sessionId,
            @RequestBody String body) {
        
        log.info("收到 MCP 消息：sessionId={}, body={}", sessionId, body);
        
        try {
            // 解析 MCP 消息
            McpMessage request = JsonUtil.fromJson(body, McpMessage.class);
            
            if (request == null) {
                return Mono.just(errorResponse(null, -32700, "Parse error"));
            }
            
            // 处理消息
            return processMessage(request)
                    .map(response -> {
                        // 通过 SSE 发送响应
                        sendSseEvent(sessionId, SseEvent.builder()
                                .event("message")
                                .data(JsonUtil.toJson(response))
                                .build());

                        // 同时返回响应（用于同步调用）
                        return (Map<String, Object>) JsonUtil.fromJson(JsonUtil.toJson(response), Map.class);
                    })
                    .onErrorResume(e -> {
                        log.error("处理 MCP 消息失败：", e);
                        McpMessage error = McpMessage.errorResponse(
                                request.getId(), -32603, e.getMessage());
                        return Mono.just((Map<String, Object>) JsonUtil.fromJson(JsonUtil.toJson(error), Map.class));
                    });
        } catch (Exception e) {
            log.error("解析 MCP 消息失败：", e);
            return Mono.just(errorResponse(null, -32700, "Parse error: " + e.getMessage()));
        }
    }

    /**
     * 处理 MCP 消息
     */
    private Mono<McpMessage> processMessage(McpMessage request) {
        String method = request.getMethod();
        Object id = request.getId();
        Map<String, Object> params = request.getParams();
        
        switch (method) {
            case "initialize":
                return serverHandler.handleInitialize(id, params);
                
            case "notifications/initialized":
                return serverHandler.handleInitialized().thenReturn(null);
                
            case "tools/list":
                return serverHandler.handleToolsList(id);
                
            case "tools/call":
                return serverHandler.handleToolsCall(id, params);
                
            case "resources/list":
                return serverHandler.handleResourcesList(id);
                
            case "resources/read":
                return serverHandler.handleResourcesRead(id, params);
                
            case "prompts/list":
                return serverHandler.handlePromptsList(id);
                
            case "prompts/get":
                return serverHandler.handlePromptsGet(id, params);
                
            default:
                return serverHandler.handleUnknownMethod(id, method);
        }
    }

    /**
     * 发送 SSE 事件
     */
    private void sendSseEvent(String sessionId, SseEvent event) {
        Sinks.Many<SseEvent> sink = sessionSinks.get(sessionId);
        if (sink != null) {
            sink.tryEmitNext(event);
        } else {
            log.warn("未找到 SSE 会话：sessionId={}", sessionId);
        }
    }

    /**
     * 创建错误响应
     */
    private Map<String, Object> errorResponse(Object id, int code, String message) {
        McpMessage error = McpMessage.errorResponse(id, code, message);
        return JsonUtil.fromJson(JsonUtil.toJson(error), Map.class);
    }

    /**
     * SSE 事件
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SseEvent {
        private String event;
        private String data;
        private String id;
    }
}
