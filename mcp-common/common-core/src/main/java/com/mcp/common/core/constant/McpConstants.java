package com.mcp.common.core.constant;

/**
 * 系统常量定义
 *
 * @author MCP Platform
 * @since 1.0.0
 */
public final class McpConstants {

    private McpConstants() {
        throw new IllegalStateException("Constants class");
    }

    /**
     * Redis Key 前缀
     */
    public static final class RedisKey {
        public static final String API_KEY_PREFIX = "mcp:api_key:";
        public static final String RATE_LIMIT_PREFIX = "mcp:rate_limit:";
        public static final String SERVICE_INSTANCE_PREFIX = "mcp:service:instance:";
        public static final String SERVICE_CACHE_PREFIX = "mcp:service:cache:";
        public static final String TOKEN_BLACKLIST_PREFIX = "mcp:token:blacklist:";
        public static final String REQUEST_LOG_PREFIX = "mcp:request:log:";
        
        private RedisKey() {
            throw new IllegalStateException("RedisKey constants class");
        }
    }

    /**
     * HTTP 请求头
     */
    public static final class Header {
        public static final String API_KEY = "X-API-Key";
        public static final String REQUEST_ID = "X-Request-ID";
        public static final String SERVICE_ID = "X-Service-ID";
        public static final String AUTHORIZATION = "Authorization";
        
        private Header() {
            throw new IllegalStateException("Header constants class");
        }
    }

    /**
     * MCP 协议常量
     */
    public static final class McpProtocol {
        public static final String PROTOCOL_VERSION = "2024-11-05";
        public static final String CONTENT_TYPE = "application/json";
        public static final String SSE_CONTENT_TYPE = "text/event-stream";
        
        // MCP 消息类型
        public static final String METHOD_INITIALIZE = "initialize";
        public static final String METHOD_INITIALIZED = "notifications/initialized";
        public static final String METHOD_TOOLS_LIST = "tools/list";
        public static final String METHOD_TOOLS_CALL = "tools/call";
        public static final String METHOD_RESOURCES_LIST = "resources/list";
        public static final String METHOD_RESOURCES_READ = "resources/read";
        public static final String METHOD_PROMPTS_LIST = "prompts/list";
        public static final String METHOD_PROMPTS_GET = "prompts/get";
        
        private McpProtocol() {
            throw new IllegalStateException("McpProtocol constants class");
        }
    }

    /**
     * 服务状态
     */
    public static final class ServiceStatus {
        public static final String REGISTERED = "REGISTERED";
        public static final String HEALTHY = "HEALTHY";
        public static final String UNHEALTHY = "UNHEALTHY";
        public static final String DISABLED = "DISABLED";
        
        private ServiceStatus() {
            throw new IllegalStateException("ServiceStatus constants class");
        }
    }

    /**
     * 认证类型
     */
    public static final class AuthType {
        public static final String API_KEY = "API_KEY";
        public static final String JWT = "JWT";
        public static final String OAUTH2 = "OAUTH2";
        
        private AuthType() {
            throw new IllegalStateException("AuthType constants class");
        }
    }

    /**
     * 限流类型
     */
    public static final class RateLimitType {
        public static final String FIXED_WINDOW = "FIXED_WINDOW";
        public static final String SLIDING_WINDOW = "SLIDING_WINDOW";
        public static final String TOKEN_BUCKET = "TOKEN_BUCKET";
        public static final String LEAKY_BUCKET = "LEAKY_BUCKET";
        
        private RateLimitType() {
            throw new IllegalStateException("RateLimitType constants class");
        }
    }

    /**
     * 响应状态码
     */
    public static final class ResponseCode {
        public static final int SUCCESS = 200;
        public static final int CREATED = 201;
        public static final int BAD_REQUEST = 400;
        public static final int UNAUTHORIZED = 401;
        public static final int FORBIDDEN = 403;
        public static final int NOT_FOUND = 404;
        public static final int METHOD_NOT_ALLOWED = 405;
        public static final int TOO_MANY_REQUESTS = 429;
        public static final int INTERNAL_ERROR = 500;
        public static final int SERVICE_UNAVAILABLE = 503;
        
        private ResponseCode() {
            throw new IllegalStateException("ResponseCode constants class");
        }
    }

    /**
     * 默认值
     */
    public static final class Defaults {
        public static final int PAGE_SIZE = 20;
        public static final int MAX_PAGE_SIZE = 100;
        public static final int RATE_LIMIT_DEFAULT = 100; // 每分钟请求数
        public static final long TOKEN_EXPIRE_HOURS = 24L;
        public static final long API_KEY_EXPIRE_DAYS = 365L;
        
        private Defaults() {
            throw new IllegalStateException("Defaults constants class");
        }
    }
}
