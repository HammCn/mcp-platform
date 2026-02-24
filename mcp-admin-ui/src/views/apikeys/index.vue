<template>
  <div class="apikeys-container page-container">
    <el-card shadow="hover">
      <!-- 操作栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="searchQuery"
            placeholder="搜索 API Key 名称..."
            style="width: 240px"
            :prefix-icon="Search"
            clearable
            @clear="loadApiKeys"
          />
        </div>
        <div class="toolbar-right">
          <el-button type="primary" :icon="Plus" @click="handleCreate">
            创建 API Key
          </el-button>
        </div>
      </div>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="apiKeys"
        style="width: 100%"
        stripe
        border
      >
        <el-table-column prop="id" label="ID" width="180" show-overflow-tooltip />
        <el-table-column prop="name" label="名称" width="200" />
        <el-table-column prop="key" label="API Key" min-width="280" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="key-display">
              <span class="key-text">{{ maskKey(row.key) }}</span>
              <el-button text type="primary" size="small" @click="handleCopy(row.key)">
                <el-icon><CopyDocument /></el-icon>
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'" size="small">
              {{ row.enabled ? '已启用' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="expiresAt" label="过期时间" width="180">
          <template #default="{ row }">
            {{ row.expiresAt ? formatTime(row.expiresAt) : '永不过期' }}
          </template>
        </el-table-column>
        <el-table-column prop="lastUsedAt" label="最后使用时间" width="180">
          <template #default="{ row }">
            {{ row.lastUsedAt ? formatTime(row.lastUsedAt) : '从未使用' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button
                size="small"
                :type="row.enabled ? 'warning' : 'success'"
                @click="handleToggleEnable(row)"
              >
                {{ row.enabled ? '禁用' : '启用' }}
              </el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)">
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadApiKeys"
          @current-change="loadApiKeys"
        />
      </div>
    </el-card>

    <!-- 创建对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="创建 API Key"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入 API Key 名称" />
        </el-form-item>
        <el-form-item label="过期时间" prop="expiresAt">
          <el-date-picker
            v-model="formData.expiresAt"
            type="datetime"
            placeholder="选择过期时间（可选）"
            style="width: 100%"
            clearable
          />
        </el-form-item>
        <el-alert
          v-if="createdKey"
          title="请妥善保存您的 API Key"
          type="warning"
          :closable="false"
          show-icon
          class="key-alert"
        >
          <div class="created-key">
            <el-input
              :model-value="createdKey"
              readonly
              type="textarea"
              :rows="3"
            />
            <el-button type="primary" size="small" @click="handleCopy(createdKey)">
              <el-icon><CopyDocument /></el-icon>
              复制
            </el-button>
          </div>
          <p>此 Key 只会显示一次，请妥善保存！</p>
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Plus, CopyDocument } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getApiKeys,
  createApiKey,
  deleteApiKey,
  enableApiKey,
  disableApiKey
} from '@/api/apikey'
import type { ApiKey, ApiKeyForm, PageResult } from '@/types'

const loading = ref(false)
const submitLoading = ref(false)
const searchQuery = ref('')
const apiKeys = ref<ApiKey[]>([])
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const createdKey = ref<string>('')

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const formData = reactive<ApiKeyForm>({
  name: '',
  expiresAt: undefined
})

const rules: FormRules = {
  name: [
    { required: true, message: '请输入 API Key 名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

const loadApiKeys = async () => {
  loading.value = true
  try {
    const res = await getApiKeys({
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    apiKeys.value = res.list
    pagination.total = res.total
  } catch (error) {
    console.error('获取 API Key 列表失败:', error)
    // 模拟数据
    apiKeys.value = [
      { id: 'key_001', key: 'sk_test_abc123def456', name: '测试 Key', enabled: true, createdAt: new Date().toISOString(), lastUsedAt: new Date().toISOString() },
      { id: 'key_002', key: 'sk_prod_xyz789uvw012', name: '生产 Key', enabled: true, expiresAt: new Date(Date.now() + 86400000 * 30).toISOString(), createdAt: new Date().toISOString(), lastUsedAt: new Date(Date.now() - 3600000).toISOString() },
      { id: 'key_003', key: 'sk_dev_mno345pqr678', name: '开发 Key', enabled: false, createdAt: new Date().toISOString() }
    ]
    pagination.total = apiKeys.value.length
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  createdKey.value = ''
  formData.name = ''
  formData.expiresAt = undefined
  dialogVisible.value = true
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
  createdKey.value = ''
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const res = await createApiKey(formData)
        createdKey.value = res.key
        ElMessage.success('创建成功，请妥善保存 API Key')
        loadApiKeys()
      } catch (error) {
        console.error('创建失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDelete = async (row: ApiKey) => {
  try {
    await ElMessageBox.confirm(`确定要删除 API Key "${row.name}" 吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteApiKey(row.id)
    ElMessage.success('删除成功')
    loadApiKeys()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const handleToggleEnable = async (row: ApiKey) => {
  try {
    if (row.enabled) {
      await disableApiKey(row.id)
      ElMessage.success('已禁用 API Key')
    } else {
      await enableApiKey(row.id)
      ElMessage.success('已启用 API Key')
    }
    loadApiKeys()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

const handleCopy = async (key: string) => {
  try {
    await navigator.clipboard.writeText(key)
    ElMessage.success('复制成功')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

const maskKey = (key: string) => {
  if (key.length <= 16) return key
  return `${key.slice(0, 8)}...${key.slice(-8)}`
}

const formatTime = (time: string) => {
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadApiKeys()
})
</script>

<style scoped lang="scss">
.apikeys-container {
  padding: $spacing-lg;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-md;
}

.pagination {
  margin-top: $spacing-md;
  display: flex;
  justify-content: flex-end;
}

.table-actions {
  display: flex;
  gap: $spacing-xs;
}

.key-display {
  display: flex;
  align-items: center;
  gap: $spacing-sm;

  .key-text {
    font-family: monospace;
    font-size: 13px;
  }
}

.key-alert {
  margin-top: $spacing-md;

  .created-key {
    display: flex;
    gap: $spacing-sm;
    margin-bottom: $spacing-sm;
  }

  p {
    margin: 0;
    font-size: 12px;
    color: $warning-color;
  }
}
</style>
