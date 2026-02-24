import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi, getCurrentUser } from '@/api/auth'

export interface UserInfo {
  id: string
  username: string
  role: string
  avatar?: string
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.username || '')
  const role = computed(() => userInfo.value?.role || '')

  /**
   * 登录
   */
  async function login(username: string, password: string, remember?: boolean) {
    const res = await loginApi(username, password, remember)
    token.value = res.token
    userInfo.value = res.user

    if (remember) {
      localStorage.setItem('token', res.token)
    } else {
      sessionStorage.setItem('token', res.token)
    }

    return res
  }

  /**
   * 获取当前用户信息
   */
  async function getUserInfo() {
    try {
      const res = await getCurrentUser()
      userInfo.value = res
      return res
    } catch (error) {
      console.error('获取用户信息失败:', error)
      throw error
    }
  }

  /**
   * 登出
   */
  async function logout() {
    try {
      await logoutApi()
    } catch (error) {
      console.error('登出失败:', error)
    } finally {
      token.value = ''
      userInfo.value = null
      localStorage.removeItem('token')
      sessionStorage.removeItem('token')
    }
  }

  /**
   * 设置 token
   */
  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  /**
   * 清除用户信息
   */
  function clearUser() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    sessionStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    username,
    role,
    login,
    getUserInfo,
    logout,
    setToken,
    clearUser
  }
})
