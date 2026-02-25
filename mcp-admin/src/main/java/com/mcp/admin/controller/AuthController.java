package com.mcp.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证 Controller
 * 提供登录、登出、获取用户信息等接口
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Tag(name = "认证管理", description = "用户认证相关 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    /**
     * 登录接口
     * 使用 Spring Security Basic 认证，登录成功后返回 token
     */
    @Operation(summary = "用户登录", description = "用户名密码登录，返回 JWT token")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, Object> loginRequest) {
        
        String username = (String) loginRequest.get("username");
        String password = (String) loginRequest.get("password");
        
        // 这里简单处理，实际应该验证密码并生成 JWT
        // 由于当前使用 Basic 认证，我们直接返回成功响应
        // 前端会在后续请求中使用 Basic 认证头
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", "Basic " + java.util.Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes()));
        
        Map<String, Object> user = new HashMap<>();
        user.put("id", "1");
        user.put("username", username);
        user.put("role", "admin");
        
        response.put("user", user);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取当前登录用户信息
     */
    @Operation(summary = "获取当前用户", description = "获取当前登录用户的信息")
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", "1");
        userInfo.put("username", authentication.getName());
        userInfo.put("role", "admin");
        
        return ResponseEntity.ok(userInfo);
    }

    /**
     * 登出
     */
    @Operation(summary = "用户登出", description = "清除登录状态")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "登出成功");
        return ResponseEntity.ok(response);
    }
}
