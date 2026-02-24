import { get, post, put, del } from './request'
import type { Service, ServiceForm, PageResult } from '@/types'

const BASE_URL = '/admin/services'

/**
 * 获取服务列表
 */
export function getServices(params?: { page?: number; pageSize?: number }) {
  return get<PageResult<Service>>(BASE_URL, params)
}

/**
 * 获取服务详情
 */
export function getServiceById(id: string) {
  return get<Service>(`${BASE_URL}/${id}`)
}

/**
 * 创建服务
 */
export function createService(data: ServiceForm) {
  return post<Service>(BASE_URL, data)
}

/**
 * 更新服务
 */
export function updateService(id: string, data: ServiceForm) {
  return put<Service>(`${BASE_URL}/${id}`, data)
}

/**
 * 删除服务
 */
export function deleteService(id: string) {
  return del(`${BASE_URL}/${id}`)
}

/**
 * 启用服务
 */
export function enableService(id: string) {
  return post(`${BASE_URL}/${id}/enable`)
}

/**
 * 禁用服务
 */
export function disableService(id: string) {
  return post(`${BASE_URL}/${id}/disable`)
}

/**
 * 刷新服务工具
 */
export function refreshServiceTools(id: string) {
  return post(`${BASE_URL}/${id}/refresh`)
}
