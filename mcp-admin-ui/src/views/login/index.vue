<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <img src="/vite.svg" alt="Logo" class="logo" />
        <h1 class="title">MCP 管理后台</h1>
        <p class="subtitle">欢迎登录</p>
      </div>

      <el-form
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <div class="form-options">
            <el-checkbox v-model="loginForm.remember">记住密码</el-checkbox>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <p>© 2026 MCP Admin. All rights reserved.</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await userStore.login(loginForm.username, loginForm.password, loginForm.remember)
        ElMessage.success('登录成功')

        const redirect = route.query.redirect as string
        router.push(redirect || '/')
      } catch (error) {
        console.error('登录失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.login-container {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 1px, transparent 1px);
    background-size: 50px 50px;
    animation: move 20s linear infinite;
  }
}

@keyframes move {
  0% {
    transform: translate(0, 0);
  }
  100% {
    transform: translate(50px, 50px);
  }
}

.login-box {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: $border-radius-large;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  z-index: 1;

  .login-header {
    text-align: center;
    margin-bottom: 32px;

    .logo {
      width: 64px;
      height: 64px;
      margin-bottom: 16px;
    }

    .title {
      font-size: 24px;
      font-weight: 600;
      color: $text-primary;
      margin-bottom: 8px;
    }

    .subtitle {
      font-size: 14px;
      color: $text-secondary;
    }
  }

  .login-form {
    .form-options {
      width: 100%;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .login-btn {
      width: 100%;
      font-size: 16px;
    }
  }

  .login-footer {
    margin-top: 24px;
    text-align: center;

    p {
      font-size: 12px;
      color: $text-placeholder;
    }
  }
}
</style>
