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
  const token = ref<string>(localStorage.getItem('token') || sessionStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(() => {
    try {
      const stored = localStorage.getItem('userInfo') || sessionStorage.getItem('userInfo')
      return stored ? JSON.parse(stored) : null
    } catch {
      return null
    }
  })()

  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.username || '')
  const role = computed(() => userInfo.value?.role || '')

  /**
   * 登录
   */
  async function login(username: string, password: string, remember?: boolean) {
    const res = await loginApi(username, password, remember)
    token.value = res.token
    
    // 获取用户信息
    try {
      userInfo.value = await getUserInfo()
    } catch (error) {
      // 如果获取用户信息失败，使用登录时返回的用户信息
      userInfo.value = res.user
    }

    if (remember) {
      localStorage.setItem('token', res.token)
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    } else {
      sessionStorage.setItem('token', res.token)
      sessionStorage.setItem('userInfo', JSON.stringify(userInfo.value))
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
