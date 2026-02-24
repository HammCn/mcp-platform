package com.mcp.common.core.exception;

import org.springframework.http.HttpStatus;

/**
 * 服务未找到异常
 *
 * @author MCP Platform
 * @since 1.0.0
 */
public class ServiceNotFoundException extends McpException {

    public ServiceNotFoundException(String serviceId) {
        super("SERVICE_NOT_FOUND", "服务不存在：" + serviceId, HttpStatus.NOT_FOUND.value());
    }

    public ServiceNotFoundException(String serviceId, String message) {
        super("SERVICE_NOT_FOUND", message, HttpStatus.NOT_FOUND.value());
    }
}
