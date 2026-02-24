package com.mcp.gateway.service;

import com.mcp.common.core.exception.ServiceNotFoundException;
import com.mcp.common.model.dto.McpTool;
import com.mcp.core.adapter.converter.OpenApiToMcpConverter;
import com.mcp.core.adapter.handler.ApiCallHandler;
import com.mcp.core.adapter.openapi.OpenApiParser;
import com.mcp.common.model.entity.McpService;
import com.mcp.common.model.entity.McpServiceInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tool 注册服务
 * 管理所有 MCP Tool 的注册和调用
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ToolRegistryService {

    private final OpenApiToMcpConverter openApiToMcpConverter;
    private final OpenApiParser openApiParser;
    private final ApiCallHandler apiCallHandler;

    /**
     * 服务工具映射：serviceCode -> List<Tool>
     */
    private final Map<String, List<McpTool>> serviceToolsMap = new ConcurrentHashMap<>();

    /**
     * 工具名称到服务的映射：toolName -> serviceCode
     */
    private final Map<String, String> toolServiceMap = new ConcurrentHashMap<>();

    /**
     * 服务实例映射：serviceCode -> List<Instance>
     */
    private final Map<String, List<McpServiceInstance>> serviceInstancesMap = new ConcurrentHashMap<>();

    /**
     * 注册服务工具
     */
    public synchronized void registerTools(String serviceCode, List<McpTool> tools) {
        // 移除旧的工具
        unregisterTools(serviceCode);
        
        // 注册新的工具
        serviceToolsMap.put(serviceCode, tools);
        
        // 建立工具到服务的映射
        for (McpTool tool : tools) {
            toolServiceMap.put(tool.getName(), serviceCode);
        }
        
        log.info("注册工具：service={}, count={}", serviceCode, tools.size());
    }

    /**
     * 注销服务工具
     */
    public synchronized void unregisterTools(String serviceCode) {
        List<McpTool> oldTools = serviceToolsMap.remove(serviceCode);
        if (oldTools != null) {
            for (McpTool tool : oldTools) {
                toolServiceMap.remove(tool.getName());
            }
        }
        log.info("注销工具：service={}", serviceCode);
    }

    /**
     * 从 OpenAPI 规范解析并注册工具
     */
    public synchronized void parseAndRegisterTools(McpService service) {
        if (service.getOpenapiSpec() == null || service.getOpenapiSpec().trim().isEmpty()) {
            log.warn("服务没有 OpenAPI 规范：{}", service.getServiceCode());
            return;
        }

        try {
            boolean isUrl = "URL".equals(service.getSpecType());
            String toolPrefix = service.getMcpToolPrefix();
            
            List<McpTool> tools = openApiToMcpConverter.convert(
                    service.getOpenapiSpec(), isUrl, toolPrefix);
            
            registerTools(service.getServiceCode(), tools);
            
            log.info("从 OpenAPI 解析并注册工具：service={}, toolCount={}", 
                    service.getServiceCode(), tools.size());
        } catch (Exception e) {
            log.error("解析 OpenAPI 规范失败：service={}", service.getServiceCode(), e);
            throw new RuntimeException("解析 OpenAPI 规范失败", e);
        }
    }

    /**
     * 获取所有工具
     */
    public List<McpTool> getAllTools() {
        return serviceToolsMap.values().stream()
                .flatMap(List::stream)
                .toList();
    }

    /**
     * 获取指定服务的工具
     */
    public List<McpTool> getServiceTools(String serviceCode) {
        return serviceToolsMap.getOrDefault(serviceCode, Collections.emptyList());
    }

    /**
     * 调用工具
     */
    public Mono<McpTool.CallResult> callTool(String toolName, Map<String, Object> arguments) {
        String serviceCode = toolServiceMap.get(toolName);
        
        if (serviceCode == null) {
            return Mono.error(new ServiceNotFoundException(toolName));
        }

        List<McpServiceInstance> instances = serviceInstancesMap.get(serviceCode);
        if (instances == null || instances.isEmpty()) {
            return Mono.error(new RuntimeException("No available service instances"));
        }

        // 选择第一个健康实例（简单轮询）
        McpServiceInstance instance = instances.stream()
                .filter(i -> "HEALTHY".equals(i.getHealthStatus()))
                .findFirst()
                .orElse(instances.get(0));

        // 解析工具名获取方法信息
        String[] parts = toolName.split("_", 2);
        String method = parts.length > 0 ? parts[0].toUpperCase() : "GET";
        String path = parts.length > 1 ? "/" + parts[1].replace("_", "/") : "/";

        // 调用 API
        return apiCallHandler.callApi(
                createMockService(serviceCode),
                instance,
                method,
                path,
                arguments,
                Collections.emptyMap()
        ).map(response -> {
            McpTool.Content content = McpTool.Content.builder()
                    .type(McpTool.ContentType.TEXT)
                    .text(response)
                    .build();
            
            return McpTool.CallResult.builder()
                    .content(Collections.singletonList(content))
                    .isError(false)
                    .build();
        });
    }

    /**
     * 注册服务实例
     */
    public synchronized void registerInstance(String serviceCode, McpServiceInstance instance) {
        serviceInstancesMap.computeIfAbsent(serviceCode, k -> new ArrayList<>())
                .add(instance);
        log.info("注册服务实例：service={}, address={}", 
                serviceCode, instance.getInstanceAddress());
    }

    /**
     * 注销服务实例
     */
    public synchronized void unregisterInstance(String serviceCode, String instanceAddress) {
        List<McpServiceInstance> instances = serviceInstancesMap.get(serviceCode);
        if (instances != null) {
            instances.removeIf(i -> instanceAddress.equals(i.getInstanceAddress()));
            log.info("注销服务实例：service={}, address={}", serviceCode, instanceAddress);
        }
    }

    /**
     * 获取服务实例列表
     */
    public List<McpServiceInstance> getServiceInstances(String serviceCode) {
        return serviceInstancesMap.getOrDefault(serviceCode, Collections.emptyList());
    }

    /**
     * 创建模拟服务对象（用于 API 调用）
     */
    private McpService createMockService(String serviceCode) {
        return McpService.builder()
                .serviceCode(serviceCode)
                .timeoutMs(30000)
                .build();
    }
}
