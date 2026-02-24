package com.mcp.gateway.repository;

import com.mcp.common.model.entity.McpServiceInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MCP 服务实例 Repository
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Repository
public interface McpServiceInstanceRepository extends JpaRepository<McpServiceInstance, Long> {

    /**
     * 按服务 ID 查找所有实例
     */
    List<McpServiceInstance> findByServiceId(Long serviceId);

    /**
     * 按服务 ID 和健康状态查找实例
     */
    List<McpServiceInstance> findByServiceIdAndHealthStatus(Long serviceId, String healthStatus);

    /**
     * 按实例地址查找
     */
    McpServiceInstance findByInstanceAddress(String instanceAddress);

    /**
     * 检查实例地址是否存在
     */
    boolean existsByInstanceAddress(String instanceAddress);

    /**
     * 按服务 ID 删除所有实例
     */
    void deleteByServiceId(Long serviceId);

    /**
     * 更新实例健康状态
     */
    @Modifying
    @Query("UPDATE McpServiceInstance i SET i.healthStatus = :status WHERE i.id = :id")
    int updateHealthStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 按服务 ID 更新所有实例状态
     */
    @Modifying
    @Query("UPDATE McpServiceInstance i SET i.status = :status WHERE i.serviceId = :serviceId")
    int updateStatusByServiceId(@Param("serviceId") Long serviceId, @Param("status") String status);
}
