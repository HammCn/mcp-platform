package com.mcp.common.core.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 MCP 基础异常
     */
    @ExceptionHandler(McpException.class)
    public ResponseEntity<Map<String, Object>> handleMcpException(McpException ex) {
        log.error("MCP 异常：code={}, message={}", ex.getCode(), ex.getMessage(), ex);
        return buildErrorResponse(ex.getCode(), ex.getMessage(), ex.getHttpStatus());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        log.warn("参数校验失败：{}", errors);
        
        Map<String, Object> body = new HashMap<>();
        body.put("code", "VALIDATION_ERROR");
        body.put("message", "参数校验失败");
        body.put("errors", errors);
        body.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, Object>> handleBindException(BindException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        
        log.warn("参数绑定失败：{}", errors);
        
        Map<String, Object> body = new HashMap<>();
        body.put("code", "BIND_ERROR");
        body.put("message", "参数绑定失败");
        body.put("errors", errors);
        body.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        log.warn("非法参数：{}", ex.getMessage());
        return buildErrorResponse("INVALID_ARGUMENT", ex.getMessage(), 
                                  HttpStatus.BAD_REQUEST.value());
    }

    /**
     * 处理其他未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        log.error("系统异常：", ex);
        return buildErrorResponse("INTERNAL_ERROR", 
                                  "系统内部错误，请稍后重试", 
                                  HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    /**
     * 构建错误响应
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            String code, String message, int httpStatus) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", code);
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.status(httpStatus).body(body);
    }
}
