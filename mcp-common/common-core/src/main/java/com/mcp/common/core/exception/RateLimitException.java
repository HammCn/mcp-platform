package com.mcp.common.core.exception;

import org.springframework.http.HttpStatus;

/**
 * 限流异常
 *
 * @author MCP Platform
 * @since 1.0.0
 */
public class RateLimitException extends McpException {

    public RateLimitException(String message) {
        super("RATE_LIMIT_EXCEEDED", message, HttpStatus.TOO_MANY_REQUESTS.value());
    }

    public RateLimitException(long retryAfterSeconds) {
        super("RATE_LIMIT_EXCEEDED", 
              "请求频率超限，请在 " + retryAfterSeconds + " 秒后重试", 
              HttpStatus.TOO_MANY_REQUESTS.value());
    }
}
