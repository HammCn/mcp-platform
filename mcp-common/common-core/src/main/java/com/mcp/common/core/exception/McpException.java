package com.mcp.common.core.exception;

import lombok.Getter;

/**
 * MCP 平台基础异常类
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Getter
public class McpException extends RuntimeException {

    /**
     * 错误码
     */
    private final String code;

    /**
     * HTTP 状态码
     */
    private final int httpStatus;

    public McpException(String message) {
        super(message);
        this.code = "INTERNAL_ERROR";
        this.httpStatus = 500;
    }

    public McpException(String code, String message) {
        super(message);
        this.code = code;
        this.httpStatus = 500;
    }

    public McpException(String code, String message, int httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public McpException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.httpStatus = 500;
    }

    public McpException(String code, String message, int httpStatus, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.httpStatus = httpStatus;
    }
}
