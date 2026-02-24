<template>
  <div class="dashboard-container page-container">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon service">
              <el-icon><Setting /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.serviceTotal }}</div>
              <div class="stat-label">服务总数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon healthy">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.serviceHealthy }}</div>
              <div class="stat-label">健康服务</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon unhealthy">
              <el-icon><CircleClose /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.serviceUnhealthy }}</div>
              <div class="stat-label">异常服务</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon key">
              <el-icon><Key /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.apiKeyTotal }}</div>
              <div class="stat-label">API Keys</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 更多内容 -->
    <el-row :gutter="16" class="content-row">
      <el-col :span="12">
        <el-card shadow="hover" class="info-card">
          <template #header>
            <div class="card-header">
              <span>API Key 统计</span>
            </div>
          </template>
          <div class="api-key-stats">
            <div class="stat-item">
              <span class="label">活跃 API Key</span>
              <span class="value active">{{ stats.apiKeyActive }}</span>
            </div>
            <div class="stat-item">
              <span class="label">已禁用 API Key</span>
              <span class="value disabled">{{ stats.apiKeyTotal - stats.apiKeyActive }}</span>
            </div>
            <el-progress
              :percentage="stats.apiKeyTotal > 0 ? (stats.apiKeyActive / stats.apiKeyTotal) * 100 : 0"
              :stroke-width="8"
              :show-text="false"
              color="#67c23a"
            />
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover" class="info-card">
          <template #header>
            <div class="card-header">
              <span>实例统计</span>
            </div>
          </template>
          <div class="instance-stats">
            <div class="stat-item">
              <span class="label">实例总数</span>
              <span class="value">{{ stats.instanceTotal }}</span>
            </div>
            <div class="stat-item">
              <span class="label">平均每服务实例</span>
              <span class="value">{{ stats.serviceTotal > 0 ? Math.round(stats.instanceTotal / stats.serviceTotal) : 0 }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近活动 -->
    <el-card shadow="hover" class="activity-card">
      <template #header>
        <div class="card-header">
          <span>最近活动</span>
          <el-button text type="primary" @click="loadActivityLogs">刷新</el-button>
        </div>
      </template>
      <el-table :data="activityLogs" style="width: 100%" :height="300">
        <el-table-column prop="action" label="操作" width="120" />
        <el-table-column prop="resource" label="资源" width="120" />
        <el-table-column prop="resourceId" label="资源 ID" width="200" show-overflow-tooltip />
        <el-table-column prop="userId" label="用户" width="100" />
        <el-table-column prop="createdAt" label="时间" min-width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Setting, CircleCheck, CircleClose, Key } from '@element-plus/icons-vue'
import { getDashboardStats, getActivityLogs } from '@/api/auth'
import type { DashboardStats, ActivityLog } from '@/types'
import { ElMessage } from 'element-plus'

const stats = ref<DashboardStats>({
  serviceTotal: 0,
  serviceHealthy: 0,
  serviceUnhealthy: 0,
  apiKeyTotal: 0,
  apiKeyActive: 0,
  instanceTotal: 0
})

const activityLogs = ref<ActivityLog[]>([])
const loading = ref(false)

const loadStats = async () => {
  try {
    const res = await getDashboardStats()
    stats.value = res
  } catch (error) {
    console.error('获取统计数据失败:', error)
    // 使用模拟数据
    stats.value = {
      serviceTotal: 12,
      serviceHealthy: 10,
      serviceUnhealthy: 2,
      apiKeyTotal: 25,
      apiKeyActive: 20,
      instanceTotal: 36
    }
  }
}

const loadActivityLogs = async () => {
  loading.value = true
  try {
    const res = await getActivityLogs({ page: 1, pageSize: 10 })
    activityLogs.value = res
  } catch (error) {
    console.error('获取活动日志失败:', error)
    // 使用模拟数据
    activityLogs.value = [
      { id: '1', action: '创建', resource: '服务', resourceId: 'svc_001', userId: 'admin', createdAt: new Date().toISOString() },
      { id: '2', action: '更新', resource: 'API Key', resourceId: 'key_002', userId: 'admin', createdAt: new Date(Date.now() - 3600000).toISOString() },
      { id: '3', action: '删除', resource: '实例', resourceId: 'inst_003', userId: 'admin', createdAt: new Date(Date.now() - 7200000).toISOString() },
      { id: '4', action: '启用', resource: '服务', resourceId: 'svc_004', userId: 'admin', createdAt: new Date(Date.now() - 86400000).toISOString() },
      { id: '5', action: '禁用', resource: 'API Key', resourceId: 'key_005', userId: 'admin', createdAt: new Date(Date.now() - 172800000).toISOString() }
    ]
  } finally {
    loading.value = false
  }
}

const formatTime = (time: string) => {
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadStats()
  loadActivityLogs()
})
</script>

<style scoped lang="scss">
.dashboard-container {
  padding: $spacing-lg;
}

.stats-row {
  margin-bottom: $spacing-lg;
}

.stat-card {
  :deep(.el-card__body) {
    padding: 20px;
  }

  .stat-content {
    display: flex;
    align-items: center;
    gap: 16px;

    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28px;
      color: #fff;

      &.service {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }

      &.healthy {
        background: linear-gradient(135deg, #56ab2f 0%, #a8e063 100%);
      }

      &.unhealthy {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      }

      &.key {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      }
    }

    .stat-info {
      flex: 1;

      .stat-value {
        font-size: 28px;
        font-weight: 600;
        color: $text-primary;
        line-height: 1;
        margin-bottom: 4px;
      }

      .stat-label {
        font-size: 14px;
        color: $text-secondary;
      }
    }
  }
}

.content-row {
  margin-bottom: $spacing-lg;
}

.info-card {
  :deep(.el-card__header) {
    padding: 16px 20px;
    border-bottom: 1px solid $border-color-lighter;
  }

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-weight: 500;
  }

  .api-key-stats,
  .instance-stats {
    padding: 8px 0;

    .stat-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid $border-color-lighter;

      &:last-of-type {
        border-bottom: none;
        margin-bottom: 12px;
      }

      .label {
        font-size: 14px;
        color: $text-regular;
      }

      .value {
        font-size: 20px;
        font-weight: 600;

        &.active {
          color: $success-color;
        }

        &.disabled {
          color: $text-secondary;
        }
      }
    }
  }
}

.activity-card {
  :deep(.el-card__header) {
    padding: 16px 20px;
    border-bottom: 1px solid $border-color-lighter;
  }

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-weight: 500;
  }
}
</style>
