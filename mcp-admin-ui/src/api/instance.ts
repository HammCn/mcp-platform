import { get, post, del } from './request'
import type { Instance, InstanceForm, PageResult } from '@/types'

const BASE_URL = '/admin/instances'

/**
 * 获取实例列表
 */
export function getInstances(params?: { page?: number; pageSize?: number; serviceId?: string }) {
  return get<PageResult<Instance>>(BASE_URL, params)
}

/**
 * 获取实例详情
 */
export function getInstanceById(id: string) {
  return get<Instance>(`${BASE_URL}/${id}`)
}

/**
 * 注册实例
 */
export function registerInstance(data: InstanceForm) {
  return post<Instance>(BASE_URL, data)
}

/**
 * 更新实例
 */
export function updateInstance(id: string, data: Partial<InstanceForm>) {
  return post<Instance>(`${BASE_URL}/${id}`, data)
}

/**
 * 注销实例
 */
export function unregisterInstance(id: string) {
  return del(`${BASE_URL}/${id}`)
}

/**
 * 更新健康状态
 */
export function updateInstanceHealth(id: string, status: 'healthy' | 'unhealthy') {
  return post(`${BASE_URL}/${id}/health`, { status })
}
