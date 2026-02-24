# MCP 管理后台

基于 Vue 3 + TypeScript + Element Plus 的 MCP 服务中台管理界面。

## 技术栈

- **Vue 3.4** - 渐进式 JavaScript 框架
- **TypeScript 5** - JavaScript 的超集
- **Element Plus** - Vue 3 组件库
- **Vite 5** - 下一代前端构建工具
- **Vue Router 4** - Vue.js 官方路由
- **Pinia** - Vue.js 状态管理库
- **Axios** - HTTP 客户端

## 功能特性

### 1. 登录页面
- 用户名/密码登录
- 表单验证
- 记住密码

### 2. 仪表盘
- 服务统计（总数、健康、异常）
- API Key 统计
- 实例统计
- 最近活动日志

### 3. 服务管理
- 服务列表展示
- 创建新服务
- 编辑服务信息
- 删除服务
- 启用/禁用服务
- 刷新服务工具
- 查看服务详情

### 4. API Key 管理
- API Key 列表展示
- 创建 API Key
- 删除 API Key
- 启用/禁用 API Key
- 一键复制 API Key

### 5. 服务实例管理
- 实例列表展示
- 按服务筛选实例
- 注册新实例
- 注销实例
- 更新实例健康状态

## 快速开始

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:3000

### 构建生产版本

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

## 项目结构

```
mcp-admin-ui/
├── src/
│   ├── api/              # API 接口
│   │   ├── request.ts    # Axios 配置
│   │   ├── service.ts    # 服务 API
│   │   ├── apikey.ts     # API Key API
│   │   └── instance.ts   # 实例 API
│   ├── components/
│   │   └── Layout/       # 布局组件
│   ├── views/            # 页面视图
│   │   ├── login/        # 登录
│   │   ├── dashboard/    # 仪表盘
│   │   ├── services/     # 服务管理
│   │   ├── apikeys/      # API Key 管理
│   │   └── instances/    # 实例管理
│   ├── router/           # 路由配置
│   ├── store/            # Pinia 状态管理
│   ├── types/            # TypeScript 类型
│   ├── assets/styles/    # 样式文件
│   ├── App.vue           # 根组件
│   └── main.ts           # 入口文件
├── index.html
├── package.json
├── vite.config.ts
└── tsconfig.json
```

## 默认登录信息

**开发环境**（Spring Security 默认）:
- 用户名：`user`
- 密码：启动日志中查看（每次启动不同）

**生产环境**需要在后端配置固定用户：

```yaml
# application.yml
spring:
  security:
    user:
      name: admin
      password: your-password
```

## API 代理配置

开发环境下，所有 `/api` 请求会代理到后端服务：

```typescript
// vite.config.ts
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true
  }
}
```

## 样式规范

使用 SCSS 和 BEM 命名规范：

```scss
// 使用 &__element 和 &--modifier
.service-list {
  &__header {
    // 头部样式
  }
  
  &__item {
    // 列表项样式
    
    &--active {
      // 激活状态
    }
  }
}
```

## 代码规范

- ESLint 代码检查
- TypeScript 严格模式
- Vue 3 Composition API + `<script setup>`

## 浏览器支持

- Chrome >= 90
- Firefox >= 88
- Safari >= 14
- Edge >= 90

## 开发指南

### 添加新页面

1. 在 `src/views/` 创建页面组件
2. 在 `src/router/index.ts` 添加路由
3. 在 `src/api/` 添加 API 接口
4. 在侧边栏菜单添加导航项

### 调用 API

```typescript
import { getServiceList } from '@/api/service'

// 在组件中调用
const { data, loading, error } = await getServiceList({
  page: 1,
  size: 20
})
```

### 状态管理

```typescript
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
userStore.setToken('your-token')
```

## 许可证

Apache License 2.0
