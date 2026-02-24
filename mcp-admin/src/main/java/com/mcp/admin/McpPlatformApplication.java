package com.mcp.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * MCP Platform 主启动类
 *
 * MCP 服务中台 - Model Context Protocol Platform
 * 提供 MCP 协议支持、OpenAPI 转换、服务注册与发现、认证授权等功能
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Slf4j
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableJpaAuditing
@EnableConfigurationProperties
@ComponentScan(basePackages = {
    "com.mcp.admin",
    "com.mcp.gateway",
    "com.mcp.core",
    "com.mcp.common"
})
@EnableJpaRepositories(basePackages = {
    "com.mcp.admin.repository",
    "com.mcp.gateway.repository",
    "com.mcp.core.repository"
})
@EntityScan(basePackages = {
    "com.mcp.admin.entity",
    "com.mcp.gateway.entity",
    "com.mcp.core.entity",
    "com.mcp.common.model.entity"
})
public class McpPlatformApplication {

    public static void main(String[] args) {
        log.info("=============================================");
        log.info("   MCP Platform 启动中...");
        log.info("   Model Context Protocol Service Platform");
        log.info("=============================================");

        SpringApplication.run(McpPlatformApplication.class, args);

        log.info("=============================================");
        log.info("   MCP Platform 启动完成!");
        log.info("   Swagger UI: http://localhost:8080/swagger-ui.html");
        log.info("   MCP SSE:    http://localhost:8080/mcp/sse");
        log.info("=============================================");
    }
}
