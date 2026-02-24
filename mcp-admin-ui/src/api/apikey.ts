import { get, post, del } from './request'
import type { ApiKey, ApiKeyForm, PageResult } from '@/types'

const BASE_URL = '/admin/api-keys'

/**
 * 获取 API Key 列表
 */
export function getApiKeys(params?: { page?: number; pageSize?: number }) {
  return get<PageResult<ApiKey>>(BASE_URL, params)
}

/**
 * 创建 API Key
 */
export function createApiKey(data: ApiKeyForm) {
  return post<ApiKey>(BASE_URL, data)
}

/**
 * 删除 API Key
 */
export function deleteApiKey(id: string) {
  return del(`${BASE_URL}/${id}`)
}

/**
 * 启用 API Key
 */
export function enableApiKey(id: string) {
  return post(`${BASE_URL}/${id}/enable`)
}

/**
 * 禁用 API Key
 */
export function disableApiKey(id: string) {
  return post(`${BASE_URL}/${id}/disable`)
}
