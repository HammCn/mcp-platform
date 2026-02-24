package com.mcp.admin.controller;

import com.mcp.common.model.dto.ApiResponse;
import com.mcp.common.model.entity.McpServiceInstance;
import com.mcp.common.model.request.RegisterInstanceRequest;
import com.mcp.admin.service.ServiceInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 服务实例管理 Controller
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Tag(name = "服务实例管理", description = "服务实例注册与发现 API")
@RestController
@RequestMapping("/api/admin/instances")
@RequiredArgsConstructor
public class ServiceInstanceController {

    private final ServiceInstanceService instanceService;

    /**
     * 注册服务实例
     */
    @Operation(summary = "注册服务实例", description = "注册新的服务实例")
    @PostMapping
    public ResponseEntity<ApiResponse<McpServiceInstance>> registerInstance(
            @Valid @RequestBody RegisterInstanceRequest request) {
        McpServiceInstance instance = instanceService.registerInstance(request);
        return ResponseEntity.ok(ApiResponse.success("实例注册成功", instance));
    }

    /**
     * 注销服务实例
     */
    @Operation(summary = "注销服务实例", description = "注销服务实例")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> unregisterInstance(@PathVariable Long id) {
        instanceService.unregisterInstance(id);
        return ResponseEntity.ok(ApiResponse.success("实例注销成功", null));
    }

    /**
     * 获取服务实例列表
     */
    @Operation(summary = "获取服务实例列表", description = "获取指定服务的所有实例")
    @GetMapping("/service/{serviceId}")
    public ResponseEntity<ApiResponse<List<McpServiceInstance>>> listInstances(
            @PathVariable Long serviceId) {
        List<McpServiceInstance> instances = instanceService.listInstances(serviceId);
        return ResponseEntity.ok(ApiResponse.success(instances));
    }

    /**
     * 更新实例健康状态
     */
    @Operation(summary = "更新实例健康状态", description = "手动更新实例健康状态")
    @PutMapping("/{id}/health")
    public ResponseEntity<ApiResponse<McpServiceInstance>> updateHealthStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        McpServiceInstance instance = instanceService.updateHealthStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("健康状态已更新", instance));
    }
}
