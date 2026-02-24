<template>
  <div class="layout-container">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside :width="sidebarWidth" class="layout-aside">
        <div class="logo">
          <img v-if="!appStore.sidebarCollapsed" src="/vite.svg" alt="Logo" class="logo-img" />
          <span v-if="!appStore.sidebarCollapsed" class="logo-text">MCP Admin</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          :collapse="appStore.sidebarCollapsed"
          :unique-opened="true"
          background-color="#001529"
          text-color="rgba(255,255,255,0.65)"
          active-text-color="#409eff"
          router
        >
          <el-menu-item index="/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <template #title>仪表盘</template>
          </el-menu-item>
          <el-menu-item index="/services">
            <el-icon><Setting /></el-icon>
            <template #title>服务管理</template>
          </el-menu-item>
          <el-menu-item index="/apikeys">
            <el-icon><Key /></el-icon>
            <template #title>API Key 管理</template>
          </el-menu-item>
          <el-menu-item index="/instances">
            <el-icon><Server /></el-icon>
            <template #title>实例管理</template>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <!-- 顶部导航栏 -->
        <el-header class="layout-header">
          <div class="header-left">
            <el-icon class="collapse-btn" @click="toggleSidebar">
              <Fold v-if="!appStore.sidebarCollapsed" />
              <Expand v-else />
            </el-icon>
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="currentRoute">{{ currentRoute.meta.title }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <el-dropdown trigger="click">
              <div class="user-info">
                <el-avatar :size="32" :icon="UserFilled" />
                <span class="username">{{ userStore.username || '管理员' }}</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleLogout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 主内容区 -->
        <el-main class="layout-main">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/store/app'
import { useUserStore } from '@/store/user'
import {
  DataAnalysis,
  Setting,
  Key,
  Server,
  Fold,
  Expand,
  UserFilled,
  ArrowDown,
  SwitchButton
} from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const sidebarWidth = computed(() => (appStore.sidebarCollapsed ? '64px' : '220px'))
const activeMenu = computed(() => route.path)
const currentRoute = computed(() => route.matched[route.matched.length - 1])

const toggleSidebar = () => {
  appStore.toggleSidebar()
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await userStore.logout()
    router.push('/login')
  } catch {
    // 取消退出
  }
}
</script>

<style scoped lang="scss">
.layout-container {
  width: 100%;
  height: 100%;
}

.el-container {
  width: 100%;
  height: 100%;
}

.layout-aside {
  background-color: #001529;
  transition: width 0.3s;
  overflow: hidden;

  .logo {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 60px;
    padding: 0 16px;
    background-color: #002140;

    .logo-img {
      width: 32px;
      height: 32px;
    }

    .logo-text {
      margin-left: 12px;
      font-size: 18px;
      font-weight: 600;
      color: #fff;
      white-space: nowrap;
    }
  }

  :deep(.el-menu) {
    border-right: none;

    .el-menu-item {
      &:hover {
        background-color: rgba(255, 255, 255, 0.08);
      }

      &.is-active {
        background-color: $primary-color;
      }
    }
  }
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: $header-bg-color;
  box-shadow: $box-shadow-light;
  padding: 0 $spacing-lg;

  .header-left {
    display: flex;
    align-items: center;
    gap: $spacing-md;

    .collapse-btn {
      font-size: 20px;
      cursor: pointer;
      transition: color 0.3s;

      &:hover {
        color: $primary-color;
      }
    }

    :deep(.el-breadcrumb) {
      line-height: 1;
    }
  }

  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;

      .username {
        font-size: 14px;
        color: $text-primary;
      }
    }
  }
}

.layout-main {
  background-color: $bg-color;
  padding: 0;
  overflow: auto;
}

// 路由过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
