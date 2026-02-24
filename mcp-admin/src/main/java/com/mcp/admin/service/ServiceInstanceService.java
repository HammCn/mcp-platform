package com.mcp.admin.service;

import com.mcp.common.core.exception.McpException;
import com.mcp.common.model.entity.McpService;
import com.mcp.common.model.entity.McpServiceInstance;
import com.mcp.common.model.request.RegisterInstanceRequest;
import com.mcp.gateway.repository.McpServiceInstanceRepository;
import com.mcp.gateway.repository.McpServiceRepository;
import com.mcp.gateway.service.ToolRegistryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务实例服务
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceInstanceService {

    private final McpServiceInstanceRepository instanceRepository;
    private final McpServiceRepository serviceRepository;
    private final ToolRegistryService toolRegistryService;

    /**
     * 注册服务实例
     */
    @Transactional
    public McpServiceInstance registerInstance(RegisterInstanceRequest request) {
        // 检查服务是否存在
        McpService service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new McpException("SERVICE_NOT_FOUND",
                        "服务不存在：" + request.getServiceId(), 404));

        // 检查实例地址是否已存在
        if (instanceRepository.existsByInstanceAddress(request.getInstanceAddress())) {
            throw new McpException("INSTANCE_EXISTS",
                    "实例地址已存在：" + request.getInstanceAddress(), 400);
        }

        McpServiceInstance instance = McpServiceInstance.builder()
                .serviceId(request.getServiceId())
                .instanceAddress(request.getInstanceAddress())
                .metadata(request.getMetadata())
                .weight(request.getWeight())
                .healthStatus("HEALTHY")
                .status("ACTIVE")
                .registeredAt(LocalDateTime.now())
                .build();

        instance = instanceRepository.save(instance);
        log.info("注册服务实例：id={}, serviceId={}, address={}",
                instance.getId(), instance.getServiceId(), instance.getInstanceAddress());

        // 注册到工具服务
        toolRegistryService.registerInstance(service.getServiceCode(), instance);

        return instance;
    }

    /**
     * 注销服务实例
     */
    @Transactional
    public void unregisterInstance(Long id) {
        McpServiceInstance instance = instanceRepository.findById(id)
                .orElseThrow(() -> new McpException("INSTANCE_NOT_FOUND",
                        "实例不存在：" + id, 404));

        // 获取服务编码
        McpService service = serviceRepository.findById(instance.getServiceId())
                .orElse(null);

        instance.setDeregisteredAt(LocalDateTime.now());
        instance.setStatus("INACTIVE");
        instanceRepository.save(instance);

        log.info("注销服务实例：id={}, address={}", instance.getId(), instance.getInstanceAddress());

        // 从工具服务注销
        if (service != null) {
            toolRegistryService.unregisterInstance(service.getServiceCode(),
                    instance.getInstanceAddress());
        }
    }

    /**
     * 获取服务实例列表
     */
    public List<McpServiceInstance> listInstances(Long serviceId) {
        return instanceRepository.findByServiceId(serviceId);
    }

    /**
     * 更新实例健康状态
     */
    @Transactional
    public McpServiceInstance updateHealthStatus(Long id, String status) {
        McpServiceInstance instance = instanceRepository.findById(id)
                .orElseThrow(() -> new McpException("INSTANCE_NOT_FOUND",
                        "实例不存在：" + id, 404));

        instance.setHealthStatus(status);
        instance.setLastHealthCheckAt(LocalDateTime.now());
        instance.setLastHealthCheckResult("Manual update");

        if ("UNHEALTHY".equals(status)) {
            instance.setConsecutiveFailures(instance.getConsecutiveFailures() + 1);
        } else {
            instance.setConsecutiveFailures(0);
        }

        instance = instanceRepository.save(instance);
        log.info("更新实例健康状态：id={}, status={}", instance.getId(), status);

        return instance;
    }

    /**
     * 健康检查
     */
    @Transactional
    public void healthCheck(Long id, boolean healthy, String result) {
        McpServiceInstance instance = instanceRepository.findById(id)
                .orElseThrow(() -> new McpException("INSTANCE_NOT_FOUND",
                        "实例不存在：" + id, 404));

        instance.setLastHealthCheckAt(LocalDateTime.now());
        instance.setLastHealthCheckResult(result);

        if (healthy) {
            instance.setHealthStatus("HEALTHY");
            instance.setConsecutiveFailures(0);
        } else {
            int failures = instance.getConsecutiveFailures() != null ?
                    instance.getConsecutiveFailures() : 0;
            instance.setConsecutiveFailures(failures + 1);

            // 连续失败 3 次标记为不健康
            if (instance.getConsecutiveFailures() >= 3) {
                instance.setHealthStatus("UNHEALTHY");
            }
        }

        instanceRepository.save(instance);
    }
}
