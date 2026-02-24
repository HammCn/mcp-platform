package com.mcp.gateway.server;

import com.mcp.common.core.util.JsonUtil;
import com.mcp.common.model.dto.McpMessage;
import com.mcp.common.model.dto.McpTool;
import com.mcp.common.model.entity.McpService;
import com.mcp.core.protocol.mcp.McpProtocol;
import com.mcp.core.protocol.transform.McpMessageTransformer;
import com.mcp.gateway.service.ToolRegistryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP Server 核心处理器
 * 处理所有 MCP 协议请求
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McpServerHandler {

    private final McpMessageTransformer messageTransformer;
    private final ToolRegistryService toolRegistryService;

    /**
     * 服务器名称
     */
    private static final String SERVER_NAME = "MCP Platform";
    
    /**
     * 服务器版本
     */
    private static final String SERVER_VERSION = "1.0.0";

    /**
     * 客户端能力缓存
     */
    private final Map<String, McpProtocol.ClientCapabilities> clientCapabilitiesCache = 
            new ConcurrentHashMap<>();

    /**
     * 处理 Initialize 请求
     */
    public Mono<McpMessage> handleInitialize(Object id, Map<String, Object> params) {
        log.info("处理 Initialize 请求");
        
        // 解析客户端能力
        McpProtocol.InitializeParams initParams = parseInitializeParams(params);
        if (initParams != null && initParams.getCapabilities() != null) {
            String clientId = getClientId(initParams.getClientInfo());
            clientCapabilitiesCache.put(clientId, initParams.getCapabilities());
        }
        
        McpMessage response = messageTransformer.createInitializeResponse(
                id, SERVER_NAME, SERVER_VERSION);
        
        return Mono.just(response);
    }

    /**
     * 处理 Initialized 通知
     */
    public Mono<Void> handleInitialized() {
        log.info("客户端已初始化完成");
        return Mono.empty();
    }

    /**
     * 处理 Tools List 请求
     */
    public Mono<McpMessage> handleToolsList(Object id) {
        log.info("处理 Tools List 请求");
        
        List<McpTool> tools = toolRegistryService.getAllTools();
        McpMessage response = messageTransformer.createToolsListResponse(id, tools);
        
        return Mono.just(response);
    }

    /**
     * 处理 Tools Call 请求
     */
    public Mono<McpMessage> handleToolsCall(Object id, Map<String, Object> params) {
        log.info("处理 Tools Call 请求：params={}", params);
        
        McpProtocol.ToolCallParams callParams = messageTransformer.parseToolCallParams(params);
        
        if (callParams.getName() == null || callParams.getName().isEmpty()) {
            return Mono.just(messageTransformer.createErrorResponse(
                    id, -32602, "Invalid params: missing tool name"));
        }
        
        return toolRegistryService.callTool(callParams.getName(), callParams.getArguments())
                .map(result -> {
                    if (result.getIsError() != null && result.getIsError()) {
                        return messageTransformer.createToolCallErrorResponse(
                                id, result.getContent().get(0).getText());
                    }
                    return messageTransformer.createToolCallSuccessResponse(
                            id, result.getContent().get(0).getText());
                })
                .onErrorResume(e -> {
                    log.error("Tool 调用失败：", e);
                    return Mono.just(messageTransformer.createToolCallErrorResponse(
                            id, "Tool execution failed: " + e.getMessage()));
                });
    }

    /**
     * 处理 Resources List 请求
     */
    public Mono<McpMessage> handleResourcesList(Object id) {
        log.info("处理 Resources List 请求");
        
        // 目前返回空列表，后续可扩展
        McpMessage response = messageTransformer.createResourcesListResponse(
                id, Collections.emptyList());
        
        return Mono.just(response);
    }

    /**
     * 处理 Resources Read 请求
     */
    public Mono<McpMessage> handleResourcesRead(Object id, Map<String, Object> params) {
        log.info("处理 Resources Read 请求：params={}", params);
        
        String uri = params != null ? (String) params.get("uri") : null;
        
        if (uri == null) {
            return Mono.just(messageTransformer.createErrorResponse(
                    id, -32602, "Invalid params: missing uri"));
        }
        
        // 目前返回空内容，后续可扩展
        McpMessage response = messageTransformer.createResourceReadResponse(id, uri, "", "text/plain");
        
        return Mono.just(response);
    }

    /**
     * 处理 Prompts List 请求
     */
    public Mono<McpMessage> handlePromptsList(Object id) {
        log.info("处理 Prompts List 请求");
        
        // 目前返回空列表，后续可扩展
        McpMessage response = messageTransformer.createPromptsListResponse(
                id, Collections.emptyList());
        
        return Mono.just(response);
    }

    /**
     * 处理 Prompts Get 请求
     */
    public Mono<McpMessage> handlePromptsGet(Object id, Map<String, Object> params) {
        log.info("处理 Prompts Get 请求：params={}", params);
        
        String name = params != null ? (String) params.get("name") : null;
        
        if (name == null) {
            return Mono.just(messageTransformer.createErrorResponse(
                    id, -32602, "Invalid params: missing prompt name"));
        }
        
        // 目前返回空内容，后续可扩展
        McpMessage response = messageTransformer.createPromptGetResponse(
                id, "Prompt: " + name, Collections.emptyList());
        
        return Mono.just(response);
    }

    /**
     * 处理未知方法
     */
    public Mono<McpMessage> handleUnknownMethod(Object id, String method) {
        log.warn("未知 MCP 方法：{}", method);
        return Mono.just(messageTransformer.createErrorResponse(
                id, -32601, "Method not found: " + method));
    }

    /**
     * 解析 Initialize 参数
     */
    private McpProtocol.InitializeParams parseInitializeParams(Map<String, Object> params) {
        if (params == null) {
            return null;
        }
        
        try {
            String json = JsonUtil.toJson(params);
            return JsonUtil.fromJson(json, McpProtocol.InitializeParams.class);
        } catch (Exception e) {
            log.warn("解析 Initialize 参数失败：", e);
            return null;
        }
    }

    /**
     * 获取客户端 ID
     */
    private String getClientId(McpProtocol.ClientInfo clientInfo) {
        if (clientInfo == null) {
            return "unknown";
        }
        return clientInfo.getName() + ":" + (clientInfo.getVersion() != null ? 
                clientInfo.getVersion() : "unknown");
    }

    /**
     * 注册服务工具
     */
    public void registerServiceTools(McpService service, List<McpTool> tools) {
        toolRegistryService.registerTools(service.getServiceCode(), tools);
        log.info("已注册服务工具：service={}, toolCount={}", 
                service.getServiceCode(), tools.size());
    }

    /**
     * 注销服务工具
     */
    public void unregisterServiceTools(String serviceCode) {
        toolRegistryService.unregisterTools(serviceCode);
        log.info("已注销服务工具：service={}", serviceCode);
    }
}
