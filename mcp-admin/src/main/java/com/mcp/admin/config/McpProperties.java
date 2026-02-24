package com.mcp.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MCP 配置属性
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "mcp")
public class McpProperties {

    /**
     * 服务器配置
     */
    private Server server = new Server();

    /**
     * 协议配置
     */
    private Protocol protocol = new Protocol();

    /**
     * SSE 配置
     */
    private Sse sse = new Sse();

    @Data
    public static class Server {
        private String name = "MCP Platform";
        private String version = "1.0.0";
        private String description = "MCP 服务中台";
    }

    @Data
    public static class Protocol {
        private String version = "2024-11-05";
    }

    @Data
    public static class Sse {
        private Boolean enabled = true;
        private String heartbeatInterval = "30s";
        private Integer maxConnections = 1000;
    }
}
