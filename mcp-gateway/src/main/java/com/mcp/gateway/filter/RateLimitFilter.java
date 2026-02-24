package com.mcp.gateway.filter;

import com.mcp.common.core.constant.McpConstants;
import com.mcp.common.core.exception.RateLimitException;
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
 * 限流过滤器
 * 基于 Redis 实现滑动窗口限流
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter implements WebFilter {

    private final StringRedisTemplate redisTemplate;

    /**
     * 默认限流：100 次/分钟
     */
    private static final int DEFAULT_LIMIT = 100;
    private static final int DEFAULT_WINDOW_SECONDS = 60;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String clientId = getClientId(exchange);
        String key = McpConstants.RedisKey.RATE_LIMIT_PREFIX + clientId;
        
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - (DEFAULT_WINDOW_SECONDS * 1000L);
        
        // 使用 Redis ZSET 实现滑动窗口
        return Mono.fromCallable(() -> {
                    // 移除窗口外的请求
                    redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);
                    
                    // 统计窗口内的请求数
                    Long count = redisTemplate.opsForZSet().zCard(key);
                    
                    if (count != null && count >= DEFAULT_LIMIT) {
                        throw new RateLimitException(getRetryAfterSeconds(key, windowStart));
                    }
                    
                    // 添加当前请求
                    redisTemplate.opsForZSet().add(key, String.valueOf(currentTime), currentTime);
                    redisTemplate.expire(key, DEFAULT_WINDOW_SECONDS, TimeUnit.SECONDS);
                    
                    return true;
                })
                .then(chain.filter(exchange))
                .onErrorResume(RateLimitException.class, e -> {
                    log.warn("请求限流：clientId={}, message={}", clientId, e.getMessage());
                    exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS);
                    exchange.getResponse().getHeaders().set("Retry-After", 
                            String.valueOf(getRetryAfterSeconds(key, windowStart)));
                    return exchange.getResponse().setComplete();
                });
    }

    /**
     * 获取客户端标识
     */
    private String getClientId(ServerWebExchange exchange) {
        // 优先使用 API Key
        String apiKey = exchange.getRequest().getHeaders()
                .getFirst(McpConstants.Header.API_KEY);
        if (apiKey != null && !apiKey.isEmpty()) {
            return "api_key:" + apiKey;
        }
        
        // 使用 IP 地址
        String clientIp = exchange.getRequest().getRemoteAddress() != null ?
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
        return "ip:" + clientIp;
    }

    /**
     * 计算重试等待时间
     */
    private long getRetryAfterSeconds(String key, long windowStart) {
        Long oldestEntry = redisTemplate.opsForZSet().range(key, 0, 0).stream()
                .findFirst()
                .map(Long::parseLong)
                .orElse(System.currentTimeMillis());
        
        long oldestTime = oldestEntry != null ? oldestEntry : System.currentTimeMillis();
        long retryAfter = ((oldestTime + DEFAULT_WINDOW_SECONDS * 1000L) - System.currentTimeMillis()) / 1000L;
        
        return Math.max(1, retryAfter);
    }
}
