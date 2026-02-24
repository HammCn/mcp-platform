package com.mcp.admin.service;

import com.mcp.common.core.exception.McpException;
import com.mcp.common.core.exception.ServiceNotFoundException;
import com.mcp.common.model.dto.PageResult;
import com.mcp.common.model.entity.McpService;
import com.mcp.common.model.request.CreateServiceRequest;
import com.mcp.common.model.response.ServiceResponse;
import com.mcp.gateway.repository.McpServiceRepository;
import com.mcp.gateway.service.ToolRegistryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * MCP 服务管理服务
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class McpServiceService {

    private final McpServiceRepository serviceRepository;
    private final ToolRegistryService toolRegistryService;

    /**
     * 创建服务
     */
    @Transactional
    public ServiceResponse createService(CreateServiceRequest request) {
        // 检查服务编码是否已存在
        if (serviceRepository.existsByServiceCode(request.getServiceCode())) {
            throw new McpException("SERVICE_CODE_EXISTS", 
                    "服务编码已存在：" + request.getServiceCode(), 400);
        }

        McpService service = McpService.builder()
                .serviceCode(request.getServiceCode())
                .serviceName(request.getServiceName())
                .description(request.getDescription())
                .version(request.getVersion())
                .serviceType(request.getServiceType())
                .baseUrl(request.getBaseUrl())
                .openapiSpec(request.getOpenapiSpec())
                .specType(request.getSpecType())
                .authType(request.getAuthType())
                .authConfig(request.getAuthConfig())
                .healthCheckPath(request.getHealthCheckPath())
                .status("REGISTERED")
                .mcpEnabled(request.getMcpEnabled())
                .mcpToolPrefix(request.getMcpToolPrefix())
                .timeoutMs(request.getTimeoutMs())
                .retryCount(request.getRetryCount())
                .build();

        service = serviceRepository.save(service);
        log.info("创建服务：id={}, code={}", service.getId(), service.getServiceCode());

        // 如果有 OpenAPI 规范，解析并注册工具
        if (request.getMcpEnabled() && request.getOpenapiSpec() != null) {
            try {
                toolRegistryService.parseAndRegisterTools(service);
                service.setStatus("HEALTHY");
                serviceRepository.save(service);
            } catch (Exception e) {
                log.error("解析 OpenAPI 规范失败：service={}", service.getServiceCode(), e);
            }
        }

        return convertToResponse(service);
    }

    /**
     * 更新服务
     */
    @Transactional
    public ServiceResponse updateService(Long id, CreateServiceRequest request) {
        McpService service = serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException(id.toString()));

        service.setServiceName(request.getServiceName());
        service.setDescription(request.getDescription());
        service.setVersion(request.getVersion());
        service.setServiceType(request.getServiceType());
        service.setBaseUrl(request.getBaseUrl());
        service.setOpenapiSpec(request.getOpenapiSpec());
        service.setSpecType(request.getSpecType());
        service.setAuthType(request.getAuthType());
        service.setAuthConfig(request.getAuthConfig());
        service.setHealthCheckPath(request.getHealthCheckPath());
        service.setMcpEnabled(request.getMcpEnabled());
        service.setMcpToolPrefix(request.getMcpToolPrefix());
        service.setTimeoutMs(request.getTimeoutMs());
        service.setRetryCount(request.getRetryCount());

        service = serviceRepository.save(service);
        log.info("更新服务：id={}, code={}", service.getId(), service.getServiceCode());

        // 如果 OpenAPI 规范有变化，重新解析工具
        if (request.getMcpEnabled() && request.getOpenapiSpec() != null) {
            try {
                toolRegistryService.parseAndRegisterTools(service);
            } catch (Exception e) {
                log.error("解析 OpenAPI 规范失败：service={}", service.getServiceCode(), e);
            }
        }

        return convertToResponse(service);
    }

    /**
     * 删除服务
     */
    @Transactional
    public void deleteService(Long id) {
        McpService service = serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException(id.toString()));

        // 注销工具
        toolRegistryService.unregisterTools(service.getServiceCode());

        serviceRepository.delete(service);
        log.info("删除服务：id={}, code={}", service.getId(), service.getServiceCode());
    }

    /**
     * 获取服务详情
     */
    public ServiceResponse getService(Long id) {
        McpService service = serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException(id.toString()));
        return convertToResponse(service);
    }

    /**
     * 获取服务列表
     */
    public PageResult<ServiceResponse> listServices(String status, String serviceType, Pageable pageable) {
        Page<McpService> page;
        
        if (status != null && serviceType != null) {
            // 需要自定义查询方法
            page = serviceRepository.findAll(pageable);
        } else if (status != null) {
            page = serviceRepository.findAll(pageable);
        } else if (serviceType != null) {
            page = serviceRepository.findAll(pageable);
        } else {
            page = serviceRepository.findAll(pageable);
        }

        List<ServiceResponse> content = page.getContent().stream()
                .map(this::convertToResponse)
                .toList();

        return PageResult.of(content, page.getTotalElements(), 
                page.getNumber() + 1, page.getSize());
    }

    /**
     * 更新服务状态
     */
    @Transactional
    public ServiceResponse updateServiceStatus(Long id, String status) {
        McpService service = serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException(id.toString()));

        service.setStatus(status);
        service = serviceRepository.save(service);

        // 如果禁用，注销工具
        if ("DISABLED".equals(status)) {
            toolRegistryService.unregisterTools(service.getServiceCode());
        }

        return convertToResponse(service);
    }

    /**
     * 刷新服务工具
     */
    @Transactional
    public void refreshServiceTools(Long id) {
        McpService service = serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException(id.toString()));

        if (service.getOpenapiSpec() == null) {
            throw new McpException("NO_OPENAPI_SPEC", "服务没有 OpenAPI 规范", 400);
        }

        toolRegistryService.parseAndRegisterTools(service);
        log.info("刷新服务工具：id={}, code={}", service.getId(), service.getServiceCode());
    }

    /**
     * 转换为响应对象
     */
    private ServiceResponse convertToResponse(McpService service) {
        return ServiceResponse.builder()
                .id(service.getId())
                .serviceCode(service.getServiceCode())
                .serviceName(service.getServiceName())
                .description(service.getDescription())
                .version(service.getVersion())
                .serviceType(service.getServiceType())
                .baseUrl(service.getBaseUrl())
                .authType(service.getAuthType())
                .status(service.getStatus())
                .mcpEnabled(service.getMcpEnabled())
                .mcpToolPrefix(service.getMcpToolPrefix())
                .createdAt(service.getCreatedAt())
                .updatedAt(service.getUpdatedAt())
                .build();
    }
}
