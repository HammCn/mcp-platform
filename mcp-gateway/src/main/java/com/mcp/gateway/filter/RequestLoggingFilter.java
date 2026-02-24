package com.mcp.gateway.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.mcp.common.core.util.IdUtil;
import com.mcp.common.core.util.JsonUtil;
import com.mcp.common.model.entity.McpRequestLog;
import com.mcp.gateway.repository.McpRequestLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 请求日志过滤器
 * 记录所有请求的详细信息
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RequestLoggingFilter implements WebFilter {

    private final McpRequestLogRepository requestLogRepository;

    private static final int MAX_BODY_SIZE = 10000;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestId = IdUtil.generateRequestId();
        long startTime = System.currentTimeMillis();

        // 记录请求信息
        McpRequestLog requestLog = McpRequestLog.builder()
                .requestId(requestId)
                .requestMethod(exchange.getRequest().getMethod().name())
                .requestPath(exchange.getRequest().getPath().value())
                .clientIp(getClientIp(exchange))
                .createdAt(LocalDateTime.now())
                .build();
        
        // 添加请求 ID 到响应头
        exchange.getResponse().getHeaders().set("X-Request-ID", requestId);
        
        return chain.filter(exchange)
                .doOnSuccess(aVoid -> {
                    long duration = System.currentTimeMillis() - startTime;
                    requestLog.setDurationMs(duration);
                    requestLog.setStatusCode(exchange.getResponse().getStatusCode() != null ? 
                            exchange.getResponse().getStatusCode().value() : 0);
                    requestLog.setSuccess(requestLog.getStatusCode() < 400);
                    saveLog(requestLog);
                })
                .doOnError(throwable -> {
                    long duration = System.currentTimeMillis() - startTime;
                    requestLog.setDurationMs(duration);
                    requestLog.setStatusCode(500);
                    requestLog.setSuccess(false);
                    requestLog.setErrorMessage(throwable.getMessage());
                    saveLog(requestLog);
                });
    }

    /**
     * 获取客户端 IP
     */
    private String getClientIp(ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        if (exchange.getRequest().getRemoteAddress() != null) {
            return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }
        
        return "unknown";
    }

    /**
     * 异步保存日志
     */
    private void saveLog(McpRequestLog requestLog) {
        // 异步保存，避免阻塞
        Mono.fromRunnable(() -> {
            try {
                requestLogRepository.save(requestLog);
            } catch (Exception e) {
                log.warn("保存请求日志失败：{}", e.getMessage());
            }
        }).subscribeOn(Schedulers.boundedElastic()).subscribe();
    }
}
