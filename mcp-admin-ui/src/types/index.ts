// 通用响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 分页参数
export interface PageParams {
  page: number
  pageSize: number
}

// 分页响应
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

// 服务类型
export interface Service {
  id: string
  name: string
  description: string
  enabled: boolean
  createdAt: string
  updatedAt: string
  instanceCount?: number
}

// 服务创建/编辑表单
export interface ServiceForm {
  name: string
  description: string
  enabled?: boolean
}

// API Key 类型
export interface ApiKey {
  id: string
  key: string
  name: string
  enabled: boolean
  expiresAt?: string
  createdAt: string
  lastUsedAt?: string
}

// API Key 创建表单
export interface ApiKeyForm {
  name: string
  expiresAt?: string
}

// 实例类型
export interface Instance {
  id: string
  serviceId: string
  serviceName?: string
  name: string
  endpoint: string
  status: 'healthy' | 'unhealthy' | 'unknown'
  lastHeartbeat?: string
  createdAt: string
}

// 实例创建表单
export interface InstanceForm {
  serviceId: string
  name: string
  endpoint: string
}

// 仪表盘统计
export interface DashboardStats {
  serviceTotal: number
  serviceHealthy: number
  serviceUnhealthy: number
  apiKeyTotal: number
  apiKeyActive: number
  instanceTotal: number
}

// 活动日志
export interface ActivityLog {
  id: string
  action: string
  resource: string
  resourceId?: string
  userId?: string
  createdAt: string
}
