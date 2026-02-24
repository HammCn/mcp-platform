package com.mcp.admin.controller;

import com.mcp.common.model.dto.ApiResponse;
import com.mcp.common.model.dto.PageResult;
import com.mcp.common.model.request.CreateApiKeyRequest;
import com.mcp.common.model.response.ApiKeyResponse;
import com.mcp.admin.service.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API Key 管理 Controller
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Tag(name = "API Key 管理", description = "API Key 认证管理 API")
@RestController
@RequestMapping("/api/admin/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    /**
     * 创建 API Key
     */
    @Operation(summary = "创建 API Key", description = "创建新的 API Key")
    @PostMapping
    public ResponseEntity<ApiResponse<ApiKeyResponse>> createApiKey(
            @Valid @RequestBody CreateApiKeyRequest request) {
        ApiKeyResponse response = apiKeyService.createApiKey(request);
        return ResponseEntity.ok(ApiResponse.success("API Key 创建成功", response));
    }

    /**
     * 删除 API Key
     */
    @Operation(summary = "删除 API Key", description = "删除 API Key")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteApiKey(@PathVariable Long id) {
        apiKeyService.deleteApiKey(id);
        return ResponseEntity.ok(ApiResponse.success("API Key 删除成功", null));
    }

    /**
     * 获取 API Key 详情
     */
    @Operation(summary = "获取 API Key 详情", description = "根据 ID 获取 API Key 详细信息")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApiKeyResponse>> getApiKey(@PathVariable Long id) {
        ApiKeyResponse response = apiKeyService.getApiKey(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取 API Key 列表
     */
    @Operation(summary = "获取 API Key 列表", description = "分页获取 API Key 列表")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<ApiKeyResponse>>> listApiKeys(
            @RequestParam(required = false) String owner,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResult<ApiKeyResponse> result = apiKeyService.listApiKeys(owner, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 启用 API Key
     */
    @Operation(summary = "启用 API Key", description = "启用 API Key")
    @PostMapping("/{id}/enable")
    public ResponseEntity<ApiResponse<ApiKeyResponse>> enableApiKey(@PathVariable Long id) {
        ApiKeyResponse response = apiKeyService.updateApiKeyStatus(id, "ACTIVE");
        return ResponseEntity.ok(ApiResponse.success("API Key 已启用", response));
    }

    /**
     * 禁用 API Key
     */
    @Operation(summary = "禁用 API Key", description = "禁用 API Key")
    @PostMapping("/{id}/disable")
    public ResponseEntity<ApiResponse<ApiKeyResponse>> disableApiKey(@PathVariable Long id) {
        ApiKeyResponse response = apiKeyService.updateApiKeyStatus(id, "INACTIVE");
        return ResponseEntity.ok(ApiResponse.success("API Key 已禁用", response));
    }

    /**
     * 刷新 API Key
     */
    @Operation(summary = "刷新 API Key", description = "生成新的 API Key")
    @PostMapping("/{id}/refresh")
    public ResponseEntity<ApiResponse<ApiKeyResponse>> refreshApiKey(@PathVariable Long id) {
        ApiKeyResponse response = apiKeyService.refreshApiKey(id);
        return ResponseEntity.ok(ApiResponse.success("API Key 已刷新", response));
    }
}
