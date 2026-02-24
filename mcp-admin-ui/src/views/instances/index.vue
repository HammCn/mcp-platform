<template>
  <div class="instances-container page-container">
    <el-card shadow="hover">
      <!-- 操作栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <el-select
            v-model="filterServiceId"
            placeholder="选择服务"
            style="width: 200px"
            clearable
            @change="loadInstances"
          >
            <el-option label="全部服务" value="" />
            <el-option
              v-for="service in services"
              :key="service.id"
              :label="service.name"
              :value="service.id"
            />
          </el-select>
          <el-input
            v-model="searchQuery"
            placeholder="搜索实例名称..."
            style="width: 240px; margin-left: 12px"
            :prefix-icon="Search"
            clearable
            @clear="loadInstances"
          />
        </div>
        <div class="toolbar-right">
          <el-button type="primary" :icon="Plus" @click="handleRegister">
            注册实例
          </el-button>
        </div>
      </div>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="instances"
        style="width: 100%"
        stripe
        border
      >
        <el-table-column prop="id" label="ID" width="180" show-overflow-tooltip />
        <el-table-column prop="serviceName" label="所属服务" width="150" />
        <el-table-column prop="name" label="实例名称" width="180" />
        <el-table-column prop="endpoint" label="端点" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="健康状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastHeartbeat" label="最后心跳" width="180">
          <template #default="{ row }">
            {{ row.lastHeartbeat ? formatTime(row.lastHeartbeat) : '无' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button
                size="small"
                type="primary"
                @click="handleUpdateHealth(row)"
              >
                更新状态
              </el-button>
              <el-button size="small" type="danger" @click="handleUnregister(row)">
                注销
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
          @size-change="loadInstances"
          @current-change="loadInstances"
        />
      </div>
    </el-card>

    <!-- 注册实例对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="注册实例"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="所属服务" prop="serviceId">
          <el-select v-model="formData.serviceId" placeholder="请选择服务" style="width: 100%">
            <el-option
              v-for="service in services"
              :key="service.id"
              :label="service.name"
              :value="service.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="实例名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入实例名称" />
        </el-form-item>
        <el-form-item label="端点地址" prop="endpoint">
          <el-input v-model="formData.endpoint" placeholder="http://localhost:3000" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 更新健康状态对话框 -->
    <el-dialog
      v-model="healthDialogVisible"
      title="更新健康状态"
      width="400px"
    >
      <el-form label-width="80px">
        <el-form-item label="当前状态">
          <el-tag :type="getStatusType(selectedInstance?.status || 'unknown')" size="small">
            {{ getStatusText(selectedInstance?.status || 'unknown') }}
          </el-tag>
        </el-form-item>
        <el-form-item label="新状态">
          <el-radio-group v-model="newHealthStatus">
            <el-radio value="healthy">健康</el-radio>
            <el-radio value="unhealthy">不健康</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="healthDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleHealthSubmit">
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
  getInstances,
  registerInstance,
  unregisterInstance,
  updateInstanceHealth
} from '@/api/instance'
import { getServices } from '@/api/service'
import type { Instance, InstanceForm, Service, PageResult } from '@/types'

const loading = ref(false)
const submitLoading = ref(false)
const searchQuery = ref('')
const filterServiceId = ref('')
const instances = ref<Instance[]>([])
const services = ref<Service[]>([])
const dialogVisible = ref(false)
const healthDialogVisible = ref(false)
const formRef = ref<FormInstance>()
const selectedInstance = ref<Instance | null>(null)
const newHealthStatus = ref<'healthy' | 'unhealthy'>('healthy')

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const formData = reactive<InstanceForm>({
  serviceId: '',
  name: '',
  endpoint: ''
})

const rules: FormRules = {
  serviceId: [
    { required: true, message: '请选择所属服务', trigger: 'change' }
  ],
  name: [
    { required: true, message: '请输入实例名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  endpoint: [
    { required: true, message: '请输入端点地址', trigger: 'blur' },
    { pattern: /^https?:\/\/.+/, message: '请输入有效的 URL 地址', trigger: 'blur' }
  ]
}

const loadServices = async () => {
  try {
    const res = await getServices({ page: 1, pageSize: 100 })
    services.value = res.list
  } catch (error) {
    console.error('获取服务列表失败:', error)
    services.value = [
      { id: 'svc_001', name: '用户服务', description: '', enabled: true, createdAt: new Date().toISOString() },
      { id: 'svc_002', name: '订单服务', description: '', enabled: true, createdAt: new Date().toISOString() },
      { id: 'svc_003', name: '支付服务', description: '', enabled: false, createdAt: new Date().toISOString() }
    ]
  }
}

const loadInstances = async () => {
  loading.value = true
  try {
    const res = await getInstances({
      page: pagination.page,
      pageSize: pagination.pageSize,
      serviceId: filterServiceId.value || undefined
    })
    instances.value = res.list
    pagination.total = res.total
  } catch (error) {
    console.error('获取实例列表失败:', error)
    // 模拟数据
    instances.value = [
      { id: 'inst_001', serviceId: 'svc_001', serviceName: '用户服务', name: '用户服务实例 1', endpoint: 'http://localhost:3001', status: 'healthy', lastHeartbeat: new Date().toISOString(), createdAt: new Date().toISOString() },
      { id: 'inst_002', serviceId: 'svc_001', serviceName: '用户服务', name: '用户服务实例 2', endpoint: 'http://localhost:3002', status: 'healthy', lastHeartbeat: new Date().toISOString(), createdAt: new Date().toISOString() },
      { id: 'inst_003', serviceId: 'svc_002', serviceName: '订单服务', name: '订单服务实例 1', endpoint: 'http://localhost:3003', status: 'unhealthy', lastHeartbeat: new Date(Date.now() - 300000).toISOString(), createdAt: new Date().toISOString() }
    ]
    pagination.total = instances.value.length
  } finally {
    loading.value = false
  }
}

const handleRegister = () => {
  formData.serviceId = ''
  formData.name = ''
  formData.endpoint = ''
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
        await registerInstance(formData)
        ElMessage.success('注册成功')
        dialogVisible.value = false
        loadInstances()
      } catch (error) {
        console.error('注册失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleUnregister = async (row: Instance) => {
  try {
    await ElMessageBox.confirm(`确定要注销实例 "${row.name}" 吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await unregisterInstance(row.id)
    ElMessage.success('注销成功')
    loadInstances()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('注销失败:', error)
    }
  }
}

const handleUpdateHealth = (row: Instance) => {
  selectedInstance.value = row
  newHealthStatus.value = row.status === 'healthy' ? 'healthy' : 'unhealthy'
  healthDialogVisible.value = true
}

const handleHealthSubmit = async () => {
  if (!selectedInstance.value) return

  submitLoading.value = true
  try {
    await updateInstanceHealth(selectedInstance.value.id, newHealthStatus.value)
    ElMessage.success('更新成功')
    healthDialogVisible.value = false
    loadInstances()
  } catch (error) {
    console.error('更新失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const getStatusType = (status: string) => {
  switch (status) {
    case 'healthy':
      return 'success'
    case 'unhealthy':
      return 'danger'
    default:
      return 'info'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'healthy':
      return '健康'
    case 'unhealthy':
      return '不健康'
    default:
      return '未知'
  }
}

const formatTime = (time: string) => {
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadServices()
  loadInstances()
})
</script>

<style scoped lang="scss">
.instances-container {
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
