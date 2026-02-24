package com.mcp.gateway.repository;

import com.mcp.common.model.entity.McpService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MCP 服务 Repository
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Repository
public interface McpServiceRepository extends JpaRepository<McpService, Long> {

    /**
     * 按服务编码查找
     */
    Optional<McpService> findByServiceCode(String serviceCode);

    /**
     * 检查服务编码是否存在
     */
    boolean existsByServiceCode(String serviceCode);

    /**
     * 按状态查找服务
     */
    List<McpService> findByStatus(String status);

    /**
     * 查找启用 MCP 的服务
     */
    @Query("SELECT s FROM McpService s WHERE s.mcpEnabled = true AND s.status = 'HEALTHY'")
    List<McpService> findMcpEnabledServices();

    /**
     * 按服务编码更新状态
     */
    int updateStatusByServiceCode(String serviceCode, String status);
}
