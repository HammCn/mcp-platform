package com.mcp.admin.controller;

import com.mcp.common.model.dto.ApiResponse;
import com.mcp.common.model.dto.PageResult;
import com.mcp.common.model.entity.McpService;
import com.mcp.common.model.request.CreateServiceRequest;
import com.mcp.common.model.response.ServiceResponse;
import com.mcp.admin.service.McpServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 服务管理 Controller
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Tag(name = "服务管理", description = "MCP 服务定义管理 API")
@RestController
@RequestMapping("/api/admin/services")
@RequiredArgsConstructor
public class ServiceController {

    private final McpServiceService serviceService;

    /**
     * 创建服务
     */
    @Operation(summary = "创建服务", description = "创建新的 MCP 服务定义")
    @PostMapping
    public ResponseEntity<ApiResponse<ServiceResponse>> createService(
            @Valid @RequestBody CreateServiceRequest request) {
        ServiceResponse response = serviceService.createService(request);
        return ResponseEntity.ok(ApiResponse.success("服务创建成功", response));
    }

    /**
     * 更新服务
     */
    @Operation(summary = "更新服务", description = "更新 MCP 服务定义")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceResponse>> updateService(
            @PathVariable Long id,
            @Valid @RequestBody CreateServiceRequest request) {
        ServiceResponse response = serviceService.updateService(id, request);
        return ResponseEntity.ok(ApiResponse.success("服务更新成功", response));
    }

    /**
     * 删除服务
     */
    @Operation(summary = "删除服务", description = "删除 MCP 服务定义")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteService(@PathVariable Long id) {
        serviceService.deleteService(id);
        return ResponseEntity.ok(ApiResponse.success("服务删除成功", null));
    }

    /**
     * 获取服务详情
     */
    @Operation(summary = "获取服务详情", description = "根据 ID 获取 MCP 服务详细信息")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceResponse>> getService(@PathVariable Long id) {
        ServiceResponse response = serviceService.getService(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取服务列表
     */
    @Operation(summary = "获取服务列表", description = "分页获取 MCP 服务列表")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<ServiceResponse>>> listServices(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String serviceType,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResult<ServiceResponse> result = serviceService.listServices(status, serviceType, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 启用服务
     */
    @Operation(summary = "启用服务", description = "启用 MCP 服务")
    @PostMapping("/{id}/enable")
    public ResponseEntity<ApiResponse<ServiceResponse>> enableService(@PathVariable Long id) {
        ServiceResponse response = serviceService.updateServiceStatus(id, "HEALTHY");
        return ResponseEntity.ok(ApiResponse.success("服务已启用", response));
    }

    /**
     * 禁用服务
     */
    @Operation(summary = "禁用服务", description = "禁用 MCP 服务")
    @PostMapping("/{id}/disable")
    public ResponseEntity<ApiResponse<ServiceResponse>> disableService(@PathVariable Long id) {
        ServiceResponse response = serviceService.updateServiceStatus(id, "DISABLED");
        return ResponseEntity.ok(ApiResponse.success("服务已禁用", response));
    }

    /**
     * 刷新服务工具
     */
    @Operation(summary = "刷新服务工具", description = "重新解析 OpenAPI 并刷新 MCP 工具列表")
    @PostMapping("/{id}/refresh")
    public ResponseEntity<ApiResponse<Void>> refreshServiceTools(@PathVariable Long id) {
        serviceService.refreshServiceTools(id);
        return ResponseEntity.ok(ApiResponse.success("工具刷新成功", null));
    }
}
