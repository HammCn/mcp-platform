package com.mcp.gateway.repository;

import com.mcp.common.model.entity.McpApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * API Key Repository
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Repository
public interface McpApiKeyRepository extends JpaRepository<McpApiKey, Long> {

    /**
     * 按 API Key 查找
     */
    Optional<McpApiKey> findByApiKey(String apiKey);

    /**
     * 检查 API Key 是否存在
     */
    boolean existsByApiKey(String apiKey);

    /**
     * 按所有者查找
     */
    List<McpApiKey> findByOwner(String owner);

    /**
     * 按状态查找
     */
    List<McpApiKey> findByStatus(String status);

    /**
     * 查找过期的 API Key
     */
    @Query("SELECT k FROM McpApiKey k WHERE k.expiresAt IS NOT NULL AND k.expiresAt < CURRENT_TIMESTAMP")
    List<McpApiKey> findExpiredKeys();

    /**
     * 更新使用次数
     */
    @Modifying
    @Query("UPDATE McpApiKey k SET k.usageCount = k.usageCount + 1, k.lastUsedAt = CURRENT_TIMESTAMP WHERE k.id = :id")
    int incrementUsageCount(@Param("id") Long id);

    /**
     * 按 API Key 更新状态
     */
    @Modifying
    @Query("UPDATE McpApiKey k SET k.status = :status WHERE k.apiKey = :apiKey")
    int updateStatusByApiKey(@Param("apiKey") String apiKey, @Param("status") String status);
}
