package com.mcp.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

/**
 * Spring Security 配置
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 配置安全过滤链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CSRF 配置 - 对 API 禁用 CSRF
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName(null);

        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
            )
            .authorizeHttpRequests(auth -> auth
                // API 端点需要认证
                .requestMatchers("/api/**").authenticated()
                // 静态资源和 actuator 端点允许访问
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // 其他所有请求需要认证
                .anyRequest().authenticated()
            )
            // 启用 Basic 认证
            .httpBasic(basic -> {})
            // 禁用 session（用于 REST API）
            .sessionManagement(session -> session.disable())
            // 禁用 frame（用于 H2 控制台，如果有使用）
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}
