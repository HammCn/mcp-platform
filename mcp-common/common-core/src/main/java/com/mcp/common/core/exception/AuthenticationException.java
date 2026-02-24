package com.mcp.common.core.exception;

import org.springframework.http.HttpStatus;

/**
 * 认证异常
 *
 * @author MCP Platform
 * @since 1.0.0
 */
public class AuthenticationException extends McpException {

    public AuthenticationException(String message) {
        super("AUTH_ERROR", message, HttpStatus.UNAUTHORIZED.value());
    }

    public AuthenticationException(String code, String message) {
        super(code, message, HttpStatus.UNAUTHORIZED.value());
    }
}
