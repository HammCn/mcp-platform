<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <div class="error-code">404</div>
      <h1 class="error-title">页面未找到</h1>
      <p class="error-description">抱歉，您访问的页面不存在</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<style scoped lang="scss">
.not-found-container {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: $bg-color;
}

.not-found-content {
  text-align: center;

  .error-code {
    font-size: 120px;
    font-weight: 700;
    color: $primary-color;
    line-height: 1;
    margin-bottom: $spacing-md;
  }

  .error-title {
    font-size: 32px;
    font-weight: 600;
    color: $text-primary;
    margin-bottom: $spacing-sm;
  }

  .error-description {
    font-size: 16px;
    color: $text-secondary;
    margin-bottom: $spacing-lg;
  }
}
</style>
