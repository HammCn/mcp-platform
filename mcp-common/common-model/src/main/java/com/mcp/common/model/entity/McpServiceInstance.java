package com.mcp.common.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * MCP 服务实例实体
 * 用于服务注册与发现，记录服务的多个实例
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Entity
@Table(name = "mcp_service_instance", indexes = {
    @Index(name = "idx_service_id", columnList = "service_id"),
    @Index(name = "idx_instance_address", columnList = "instance_address"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class McpServiceInstance {

    /**
     * 主键 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 服务 ID
     */
    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    /**
     * 实例地址（IP:Port）
     */
    @Column(name = "instance_address", length = 256, nullable = false)
    private String instanceAddress;

    /**
     * 实例元数据（JSON 格式）
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    /**
     * 权重（用于负载均衡）
     */
    @Column(name = "weight")
    @Builder.Default
    private Integer weight = 100;

    /**
     * 健康状态：HEALTHY, UNHEALTHY
     */
    @Column(name = "health_status", length = 32, nullable = false)
    @Builder.Default
    private String healthStatus = "HEALTHY";

    /**
     * 最后健康检查时间
     */
    @Column(name = "last_health_check_at")
    private LocalDateTime lastHealthCheckAt;

    /**
     * 最后健康检查结果
     */
    @Column(name = "last_health_check_result", length = 1024)
    private String lastHealthCheckResult;

    /**
     * 连续健康检查失败次数
     */
    @Column(name = "consecutive_failures")
    @Builder.Default
    private Integer consecutiveFailures = 0;

    /**
     * 实例状态：ACTIVE, INACTIVE
     */
    @Column(name = "status", length = 32, nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    /**
     * 注册时间
     */
    @CreationTimestamp
    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 注销时间
     */
    @Column(name = "deregistered_at")
    private LocalDateTime deregisteredAt;
}
