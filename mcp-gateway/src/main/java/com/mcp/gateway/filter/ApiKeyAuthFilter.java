package com.mcp.gateway.filter;

import com.mcp.common.core.constant.McpConstants;
import com.mcp.common.core.exception.AuthenticationException;
import com.mcp.common.model.entity.McpApiKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * API Key 认证过滤器
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyAuthFilter implements WebFilter {

    private final StringRedisTemplate redisTemplate;

    /**
     * 不需要认证的路径
     */
    private static final String[] EXCLUDED_PATHS = {
            "/mcp/sse",
            "/mcp/message",
            "/actuator",
            "/swagger",
            "/v3/api-docs"
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        
        // 检查是否需要认证
        if (isExcluded(path)) {
            return chain.filter(exchange);
        }
        
        String apiKey = exchange.getRequest().getHeaders()
                .getFirst(McpConstants.Header.API_KEY);
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            // 对于 MCP 端点，允许无认证连接（在消息处理时再验证）
            if (path.startsWith("/mcp")) {
                return chain.filter(exchange);
            }
            throw new AuthenticationException("API_KEY_MISSING", "缺少 API Key");
        }
        
        return validateApiKey(apiKey)
                .flatMap(valid -> {
                    if (!valid) {
                        throw new AuthenticationException("API_KEY_INVALID", "无效的 API Key");
                    }
                    // 将 API Key 信息放入请求属性
                    exchange.getAttributes().put("apiKey", apiKey);
                    return chain.filter(exchange);
                });
    }

    /**
     * 验证 API Key
     */
    private Mono<Boolean> validateApiKey(String apiKey) {
        String cacheKey = McpConstants.RedisKey.API_KEY_PREFIX + apiKey;
        
        return Mono.fromCallable(() -> {
                    String cached = redisTemplate.opsForValue().get(cacheKey);
                    
                    if (cached != null) {
                        // 已缓存，验证通过
                        return true;
                    }
                    
                    // 从数据库查询（实际应用中应该调用服务层）
                    // 这里简化处理，假设所有格式正确的 API Key 都有效
                    if (apiKey.startsWith("sk_")) {
                        // 缓存 API Key 有效性（5 分钟）
                        redisTemplate.opsForValue().set(cacheKey, "valid", 5, TimeUnit.MINUTES);
                        return true;
                    }
                    
                    return false;
                })
                .onErrorReturn(false);
    }

    /**
     * 检查路径是否排除
     */
    private boolean isExcluded(String path) {
        for (String excluded : EXCLUDED_PATHS) {
            if (path.startsWith(excluded)) {
                return true;
            }
        }
        return false;
    }
}
