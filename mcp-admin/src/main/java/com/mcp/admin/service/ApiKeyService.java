package com.mcp.admin.service;

import com.mcp.common.core.exception.McpException;
import com.mcp.common.core.util.IdUtil;
import com.mcp.common.model.dto.PageResult;
import com.mcp.common.model.entity.McpApiKey;
import com.mcp.common.model.request.CreateApiKeyRequest;
import com.mcp.common.model.response.ApiKeyResponse;
import com.mcp.gateway.repository.McpApiKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * API Key 服务
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final McpApiKeyRepository apiKeyRepository;

    /**
     * 创建 API Key
     */
    @Transactional
    public ApiKeyResponse createApiKey(CreateApiKeyRequest request) {
        String apiKey = IdUtil.generateApiKey();

        McpApiKey key = McpApiKey.builder()
                .apiKey(apiKey)
                .name(request.getName())
                .description(request.getDescription())
                .owner(request.getOwner())
                .allowedServices(request.getAllowedServices() != null ? 
                        String.join(",", request.getAllowedServices()) : null)
                .rateLimitConfigId(request.getRateLimitConfigId())
                .expiresAt(request.getExpiresAt())
                .status("ACTIVE")
                .build();

        key = apiKeyRepository.save(key);
        log.info("创建 API Key: id={}, name={}", key.getId(), key.getName());

        return convertToResponse(key, apiKey);
    }

    /**
     * 删除 API Key
     */
    @Transactional
    public void deleteApiKey(Long id) {
        McpApiKey key = apiKeyRepository.findById(id)
                .orElseThrow(() -> new McpException("API_KEY_NOT_FOUND", 
                        "API Key 不存在：" + id, 404));

        apiKeyRepository.delete(key);
        log.info("删除 API Key: id={}, name={}", key.getId(), key.getName());
    }

    /**
     * 获取 API Key 详情
     */
    public ApiKeyResponse getApiKey(Long id) {
        McpApiKey key = apiKeyRepository.findById(id)
                .orElseThrow(() -> new McpException("API_KEY_NOT_FOUND", 
                        "API Key 不存在：" + id, 404));
        return convertToResponse(key, null);
    }

    /**
     * 获取 API Key 列表
     */
    public PageResult<ApiKeyResponse> listApiKeys(String owner, String status, Pageable pageable) {
        Page<McpApiKey> page;

        if (owner != null && status != null) {
            // 需要自定义查询
            page = apiKeyRepository.findAll(pageable);
        } else if (owner != null) {
            // 需要自定义查询
            page = apiKeyRepository.findAll(pageable);
        } else if (status != null) {
            // 需要自定义查询
            page = apiKeyRepository.findAll(pageable);
        } else {
            page = apiKeyRepository.findAll(pageable);
        }

        List<ApiKeyResponse> content = page.getContent().stream()
                .map(key -> convertToResponse(key, null))
                .toList();

        return PageResult.of(content, page.getTotalElements(),
                page.getNumber() + 1, page.getSize());
    }

    /**
     * 更新 API Key 状态
     */
    @Transactional
    public ApiKeyResponse updateApiKeyStatus(Long id, String status) {
        McpApiKey key = apiKeyRepository.findById(id)
                .orElseThrow(() -> new McpException("API_KEY_NOT_FOUND",
                        "API Key 不存在：" + id, 404));

        key.setStatus(status);
        key = apiKeyRepository.save(key);

        return convertToResponse(key, null);
    }

    /**
     * 刷新 API Key
     */
    @Transactional
    public ApiKeyResponse refreshApiKey(Long id) {
        McpApiKey key = apiKeyRepository.findById(id)
                .orElseThrow(() -> new McpException("API_KEY_NOT_FOUND",
                        "API Key 不存在：" + id, 404));

        String newApiKey = IdUtil.generateApiKey();
        key.setApiKey(newApiKey);
        key = apiKeyRepository.save(key);

        log.info("刷新 API Key: id={}, name={}", key.getId(), key.getName());

        return convertToResponse(key, newApiKey);
    }

    /**
     * 验证 API Key
     */
    public boolean validateApiKey(String apiKey) {
        return apiKeyRepository.findByApiKey(apiKey)
                .filter(McpApiKey::isValid)
                .isPresent();
    }

    /**
     * 转换为响应对象
     */
    private ApiKeyResponse convertToResponse(McpApiKey key, String apiKeyValue) {
        return ApiKeyResponse.builder()
                .id(key.getId())
                .apiKey(apiKeyValue)  // 仅在创建/刷新时返回
                .name(key.getName())
                .description(key.getDescription())
                .owner(key.getOwner())
                .allowedServices(key.getAllowedServices())
                .expiresAt(key.getExpiresAt())
                .lastUsedAt(key.getLastUsedAt())
                .usageCount(key.getUsageCount())
                .status(key.getStatus())
                .createdAt(key.getCreatedAt())
                .build();
    }
}
