import { get, post } from './request'
import type { DashboardStats, ActivityLog } from '@/types'

const BASE_URL = '/admin/dashboard'

/**
 * 获取仪表盘统计数据
 */
export function getDashboardStats() {
  return get<DashboardStats>(`${BASE_URL}/stats`)
}

/**
 * 获取活动日志
 */
export function getActivityLogs(params?: { page?: number; pageSize?: number }) {
  return get<ActivityLog[]>(`${BASE_URL}/logs`, params)
}

/**
 * 登录
 */
export function login(username: string, password: string, remember?: boolean) {
  return post<{ token: string; user: { id: string; username: string; role: string } }>(
    '/auth/login',
    { username, password, remember }
  )
}

/**
 * 获取当前用户信息
 */
export function getCurrentUser() {
  return get('/auth/me')
}

/**
 * 登出
 */
export function logout() {
  return post('/auth/logout')
}
