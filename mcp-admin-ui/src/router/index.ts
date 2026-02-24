import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/components/Layout/index.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'DataAnalysis' }
      },
      {
        path: 'services',
        name: 'Services',
        component: () => import('@/views/services/index.vue'),
        meta: { title: '服务管理', icon: 'Setting' }
      },
      {
        path: 'apikeys',
        name: 'ApiKeys',
        component: () => import('@/views/apikeys/index.vue'),
        meta: { title: 'API Key 管理', icon: 'Key' }
      },
      {
        path: 'instances',
        name: 'Instances',
        component: () => import('@/views/instances/index.vue'),
        meta: { title: '实例管理', icon: 'Server' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/404/index.vue'),
    meta: { title: '404' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()

  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - MCP 管理后台` : 'MCP 管理后台'

  // 检查是否需要登录
  if (to.meta.requiresAuth !== false) {
    if (!userStore.isLoggedIn) {
      // 未登录，跳转到登录页
      next(`/login?redirect=${to.path}`)
      return
    }

    // 尝试获取用户信息（如果还没有）
    if (!userStore.userInfo) {
      try {
        await userStore.getUserInfo()
      } catch (error) {
        // 获取用户信息失败，清除登录状态
        userStore.clearUser()
        next(`/login?redirect=${to.path}`)
        return
      }
    }
  } else {
    // 如果已登录且访问登录页，重定向到首页
    if (to.path === '/login' && userStore.isLoggedIn) {
      next(from.query.redirect as string || '/')
      return
    }
  }

  next()
})

export default router
