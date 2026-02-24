package com.mcp.gateway.repository;

import com.mcp.common.model.entity.McpRequestLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 请求日志 Repository
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Repository
public interface McpRequestLogRepository extends JpaRepository<McpRequestLog, Long> {

    /**
     * 按请求 ID 查找
     */
    McpRequestLog findByRequestId(String requestId);

    /**
     * 按服务 ID 分页查询
     */
    Page<McpRequestLog> findByServiceId(Long serviceId, Pageable pageable);

    /**
     * 按服务编码分页查询
     */
    Page<McpRequestLog> findByServiceCode(String serviceCode, Pageable pageable);

    /**
     * 按时间范围查询
     */
    @Query("SELECT l FROM McpRequestLog l WHERE l.createdAt BETWEEN :startTime AND :endTime")
    Page<McpRequestLog> findByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    /**
     * 统计成功请求数
     */
    @Query("SELECT COUNT(l) FROM McpRequestLog l WHERE l.serviceId = :serviceId AND l.success = true AND l.createdAt >= :startTime")
    long countSuccessfulRequests(@Param("serviceId") Long serviceId, @Param("startTime") LocalDateTime startTime);

    /**
     * 统计失败请求数
     */
    @Query("SELECT COUNT(l) FROM McpRequestLog l WHERE l.serviceId = :serviceId AND l.success = false AND l.createdAt >= :startTime")
    long countFailedRequests(@Param("serviceId") Long serviceId, @Param("startTime") LocalDateTime startTime);

    /**
     * 按服务 ID 和时间范围删除旧日志
     */
    void deleteByCreatedAtBefore(LocalDateTime dateTime);
}
