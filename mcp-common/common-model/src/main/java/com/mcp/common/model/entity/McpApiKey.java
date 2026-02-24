package com.mcp.common.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * API Key 认证实体
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Entity
@Table(name = "mcp_api_key", indexes = {
    @Index(name = "idx_api_key", columnList = "api_key"),
    @Index(name = "idx_owner", columnList = "owner"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class McpApiKey {

    /**
     * 主键 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * API Key（加密存储）
     */
    @Column(name = "api_key", length = 128, nullable = false, unique = true)
    private String apiKey;

    /**
     * API Key 名称
     */
    @Column(name = "name", length = 128, nullable = false)
    private String name;

    /**
     * API Key 描述
     */
    @Column(name = "description", length = 512)
    private String description;

    /**
     * 所有者（用户 ID 或系统标识）
     */
    @Column(name = "owner", length = 64, nullable = false)
    private String owner;

    /**
     * 允许访问的服务列表（JSON 数组，空表示所有服务）
     */
    @Column(name = "allowed_services", columnDefinition = "TEXT")
    private String allowedServices;

    /**
     * 限流配置 ID
     */
    @Column(name = "rate_limit_config_id")
    private Long rateLimitConfigId;

    /**
     * 过期时间
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * 最后使用时间
     */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    /**
     * 使用次数统计
     */
    @Column(name = "usage_count")
    @Builder.Default
    private Long usageCount = 0L;

    /**
     * 状态：ACTIVE, INACTIVE, EXPIRED
     */
    @Column(name = "status", length = 32, nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 创建人
     */
    @Column(name = "created_by", length = 64)
    private String createdBy;

    /**
     * 检查 API Key 是否有效
     */
    public boolean isValid() {
        return "ACTIVE".equals(status) && 
               (expiresAt == null || expiresAt.isAfter(LocalDateTime.now()));
    }
}
