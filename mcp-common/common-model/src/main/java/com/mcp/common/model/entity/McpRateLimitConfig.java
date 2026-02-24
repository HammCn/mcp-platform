package com.mcp.common.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 限流配置实体
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Entity
@Table(name = "mcp_rate_limit_config", indexes = {
    @Index(name = "idx_config_type", columnList = "config_type"),
    @Index(name = "idx_target_id", columnList = "target_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class McpRateLimitConfig {

    /**
     * 主键 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 配置名称
     */
    @Column(name = "name", length = 128, nullable = false)
    private String name;

    /**
     * 配置描述
     */
    @Column(name = "description", length = 512)
    private String description;

    /**
     * 配置类型：GLOBAL, SERVICE, API_KEY, IP
     */
    @Column(name = "config_type", length = 32, nullable = false)
    private String configType;

    /**
     * 目标 ID（服务 ID、API Key ID 等，根据类型而定）
     */
    @Column(name = "target_id", length = 64)
    private String targetId;

    /**
     * 限流算法：FIXED_WINDOW, SLIDING_WINDOW, TOKEN_BUCKET, LEAKY_BUCKET
     */
    @Column(name = "algorithm", length = 32, nullable = false)
    private String algorithm;

    /**
     * 请求限制数量
     */
    @Column(name = "limit", nullable = false)
    private Integer limit;

    /**
     * 时间窗口（秒）
     */
    @Column(name = "window_seconds", nullable = false)
    private Integer windowSeconds;

    /**
     * Token 桶容量（仅 TOKEN_BUCKET 算法）
     */
    @Column(name = "bucket_capacity")
    private Integer bucketCapacity;

    /**
     * Token 补充速率（个/秒，仅 TOKEN_BUCKET 算法）
     */
    @Column(name = "refill_rate")
    private Double refillRate;

    /**
     * 是否启用
     */
    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    /**
     * 限流响应消息
     */
    @Column(name = "reject_message", length = 256)
    private String rejectMessage;

    /**
     * 限流响应 HTTP 状态码
     */
    @Column(name = "reject_status_code")
    @Builder.Default
    private Integer rejectStatusCode = 429;

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
     * 更新人
     */
    @Column(name = "updated_by", length = 64)
    private String updatedBy;
}
