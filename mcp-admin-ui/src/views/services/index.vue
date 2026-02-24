<template>
  <div class="services-container page-container">
    <el-card shadow="hover">
      <!-- 操作栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="searchQuery"
            placeholder="搜索服务名称..."
            style="width: 240px"
            :prefix-icon="Search"
            clearable
            @clear="loadServices"
          />
        </div>
        <div class="toolbar-right">
          <el-button type="primary" :icon="Plus" @click="handleCreate">
            创建服务
          </el-button>
        </div>
      </div>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="services"
        style="width: 100%"
        stripe
        border
      >
        <el-table-column prop="id" label="ID" width="180" show-overflow-tooltip />
        <el-table-column prop="name" label="服务名称" width="200" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="instanceCount" label="实例数" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.instanceCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'" size="small">
              {{ row.enabled ? '已启用' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button
                size="small"
                :type="row.enabled ? 'warning' : 'success'"
                @click="handleToggleEnable(row)"
              >
                {{ row.enabled ? '禁用' : '启用' }}
              </el-button>
              <el-button size="small" @click="handleRefresh(row)">
                刷新
              </el-button>
              <el-button size="small" type="primary" @click="handleEdit(row)">
                编辑
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
          @size-change="loadServices"
          @current-change="loadServices"
        />
      </div>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑服务' : '创建服务'"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="服务名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入服务名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入服务描述"
          />
        </el-form-item>
        <el-form-item label="启用状态" prop="enabled">
          <el-switch v-model="formData.enabled" />
        </el-form-item>
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
import { Search, Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getServices,
  createService,
  updateService,
  deleteService,
  enableService,
  disableService,
  refreshServiceTools
} from '@/api/service'
import type { Service, ServiceForm, PageResult } from '@/types'

const loading = ref(false)
const submitLoading = ref(false)
const searchQuery = ref('')
const services = ref<Service[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const formData = reactive<ServiceForm>({
  name: '',
  description: '',
  enabled: true
})

const rules: FormRules = {
  name: [
    { required: true, message: '请输入服务名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入服务描述', trigger: 'blur' }
  ]
}

const loadServices = async () => {
  loading.value = true
  try {
    const res = await getServices({
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    services.value = res.list
    pagination.total = res.total
  } catch (error) {
    console.error('获取服务列表失败:', error)
    // 模拟数据
    services.value = [
      { id: 'svc_001', name: '用户服务', description: '处理用户相关的业务逻辑', enabled: true, createdAt: new Date().toISOString(), instanceCount: 3 },
      { id: 'svc_002', name: '订单服务', description: '处理订单创建、查询等操作', enabled: true, createdAt: new Date().toISOString(), instanceCount: 2 },
      { id: 'svc_003', name: '支付服务', description: '处理支付相关的业务', enabled: false, createdAt: new Date().toISOString(), instanceCount: 1 }
    ]
    pagination.total = services.value.length
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  isEdit.value = false
  formData.name = ''
  formData.description = ''
  formData.enabled = true
  dialogVisible.value = true
}

const handleEdit = (row: Service) => {
  isEdit.value = true
  formData.name = row.name
  formData.description = row.description
  formData.enabled = row.enabled
  dialogVisible.value = true
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value) {
          await updateService('test-id', formData)
          ElMessage.success('更新成功')
        } else {
          await createService(formData)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadServices()
      } catch (error) {
        console.error('操作失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDelete = async (row: Service) => {
  try {
    await ElMessageBox.confirm(`确定要删除服务 "${row.name}" 吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteService(row.id)
    ElMessage.success('删除成功')
    loadServices()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const handleToggleEnable = async (row: Service) => {
  try {
    if (row.enabled) {
      await disableService(row.id)
      ElMessage.success('已禁用服务')
    } else {
      await enableService(row.id)
      ElMessage.success('已启用服务')
    }
    loadServices()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

const handleRefresh = async (row: Service) => {
  try {
    await refreshServiceTools(row.id)
    ElMessage.success('刷新成功')
  } catch (error) {
    console.error('刷新失败:', error)
  }
}

const formatTime = (time: string) => {
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadServices()
})
</script>

<style scoped lang="scss">
.services-container {
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
</style>
