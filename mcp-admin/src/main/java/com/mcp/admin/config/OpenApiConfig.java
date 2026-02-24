package com.mcp.admin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger 配置
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:MCP Platform}")
    private String applicationName;

    /**
     * 配置 OpenAPI 文档
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MCP Platform API")
                        .version("1.0.0")
                        .description("MCP 服务中台 API 文档\n\n" +
                                "## 功能说明\n\n" +
                                "- **服务管理**: MCP 服务的注册、配置和管理\n" +
                                "- **API Key 管理**: API Key 的创建、吊销和权限管理\n" +
                                "- **服务实例**: 服务实例的注册与发现\n" +
                                "- **MCP 协议**: 支持 MCP 协议的 SSE 传输\n\n" +
                                "## 认证方式\n\n" +
                                "1. **API Key**: 在请求头中添加 `X-API-Key`\n" +
                                "2. **JWT**: 在请求头中添加 `Authorization: Bearer <token>`")
                        .contact(new Contact()
                                .name("MCP Platform")
                                .email("support@mcp-platform.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("本地开发环境"),
                        new Server().url("http://localhost:8080").description("生产环境")))
                .addSecurityItem(new SecurityRequirement().addList("ApiKey"))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .schemaRequirement("ApiKey", createApiKeySecurityScheme())
                .schemaRequirement("BearerAuth", createBearerSecurityScheme());
    }

    /**
     * API Key 安全方案
     */
    private SecurityScheme createApiKeySecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .name("X-API-Key")
                .in(SecurityScheme.In.HEADER)
                .description("API Key 认证，在请求头中添加 `X-API-Key: <your-api-key>`");
    }

    /**
     * Bearer Token 安全方案
     */
    private SecurityScheme createBearerSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT Token 认证，在请求头中添加 `Authorization: Bearer <token>`");
    }
}
