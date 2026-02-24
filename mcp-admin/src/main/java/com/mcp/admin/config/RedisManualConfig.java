package com.mcp.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis 配置
 * 手动创建 Redis Bean
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Configuration
public class RedisManualConfig {

    /**
     * 创建 Redis 连接工厂
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // 配置 Redis 连接，如果 Redis 不可用，连接会失败但不会阻止应用启动
        return new LettuceConnectionFactory("localhost", 6379);
    }

    /**
     * 手动创建 StringRedisTemplate
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate() {
        return new StringRedisTemplate(redisConnectionFactory());
    }
}
