import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 侧边栏是否折叠
  const sidebarCollapsed = ref<boolean>(false)

  // 是否显示设置面板
  const showSettings = ref<boolean>(false)

  // 主题色
  const themeColor = ref<string>('#409eff')

  // 页面标题
  const pageTitle = ref<string>('')

  /**
   * 切换侧边栏
   */
  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  /**
   * 设置侧边栏状态
   */
  function setSidebarCollapsed(collapsed: boolean) {
    sidebarCollapsed.value = collapsed
  }

  /**
   * 设置页面标题
   */
  function setPageTitle(title: string) {
    pageTitle.value = title
  }

  /**
   * 设置主题色
   */
  function setThemeColor(color: string) {
    themeColor.value = color
  }

  return {
    sidebarCollapsed,
    showSettings,
    themeColor,
    pageTitle,
    toggleSidebar,
    setSidebarCollapsed,
    setPageTitle,
    setThemeColor
  }
})
