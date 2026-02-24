# MCP Platform - MCP 服务中台

[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2+-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

MCP (Model Context Protocol) 服务中台是一个基于 MCP 协议的服务集成平台，提供 OpenAPI 转换、服务注册与发现、认证授权、限流等功能。

## 📋 目录

- [功能特性](#-功能特性)
- [技术栈](#-技术栈)
- [项目结构](#-项目结构)
- [快速开始](#-快速开始)
- [配置说明](#-配置说明)
- [API 文档](#-api-文档)
- [MCP 协议](#-mcp-协议)
- [常见问题](#-常见问题)

## ✨ 功能特性

### 核心功能
- **MCP 协议支持**: 完整实现 MCP 协议，支持 Tools、Resources、Prompts
- **OpenAPI 转换**: 自动将 OpenAPI 规范转换为 MCP Tools
- **服务注册与发现**: 支持服务实例注册、健康检查、负载均衡
- **认证授权**: API Key、JWT 多种认证方式
- **限流控制**: 基于 Redis 的滑动窗口限流

### 管理功能
- **服务管理**: 服务的 CRUD、启用/禁用、工具刷新
- **API Key 管理**: API Key 的创建、吊销、权限控制
- **实例管理**: 服务实例的注册、注销、健康状态管理
- **请求日志**: 完整的请求日志记录和查询

### 可观测性
- **健康检查**: Spring Actuator 健康检查端点
- **指标监控**: Prometheus 指标导出
- **请求追踪**: 基于 Request ID 的请求追踪

## 🛠 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 基础语言 |
| Spring Boot | 3.2.4 | 应用框架 |
| Spring Data JPA | - | ORM 框架 |
| MySQL | 8.0+ | 关系数据库 |
| Redis | 6.0+ | 缓存/限流 |
| Swagger Parser | 2.1.20 | OpenAPI 解析 |
| SpringDoc | 2.3.0 | API 文档 |

## 📁 项目结构

```
mcp-platform/
├── mcp-common/                    # 公共模块
│   ├── common-core/               # 核心工具类、异常处理
│   │   ├── constant/              # 常量定义
│   │   ├── exception/             # 异常类
│   │   └── util/                  # 工具类
│   └── common-model/              # 数据模型
│       ├── entity/                # JPA 实体
│       ├── dto/                   # 数据传输对象
│       ├── request/               # 请求对象
│       └── response/              # 响应对象
├── mcp-core/                      # 核心服务模块
│   ├── api-adapter/               # API 适配器
│   │   ├── openapi/               # OpenAPI 解析
│   │   ├── converter/             # 协议转换
│   │   └── handler/               # 调用处理
│   └── protocol-converter/        # 协议转换器
│       ├── mcp/                   # MCP 协议模型
│       └── transform/             # 消息转换
├── mcp-gateway/                   # MCP 网关模块
│   ├── server/                    # MCP Server 实现
│   ├── handler/                   # 请求处理
│   ├── filter/                    # 过滤器
│   ├── service/                   # 服务层
│   └── repository/                # 数据访问层
├── mcp-admin/                     # 管理后台模块
│   ├── controller/                # REST Controller
│   ├── service/                   # 业务服务
│   ├── config/                    # 配置类
│   └── McpPlatformApplication.java # 启动类
├── database/                      # 数据库脚本
│   └── schema.sql                 # 建表脚本
└── pom.xml                        # 父 POM
```

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+

### 1. 数据库初始化

```bash
# 创建数据库并执行建表脚本
mysql -u root -p < database/schema.sql
```

### 2. 配置修改

编辑 `mcp-admin/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mcp_platform
    username: root
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
```

### 3. 启动应用

```bash
# 进入项目目录
cd mcp-platform

# 编译并启动
mvn clean install
mvn spring-boot:run -pl mcp-admin
```

### 4. 验证启动

访问以下地址验证服务是否正常：

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **健康检查**: http://localhost:8080/actuator/health
- **Prometheus 指标**: http://localhost:8080/actuator/prometheus

## ⚙️ 配置说明

### 主要配置项

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `server.port` | 8080 | 服务端口 |
| `spring.datasource.url` | - | 数据库连接 URL |
| `spring.data.redis.host` | localhost | Redis 主机 |
| `mcp.server.name` | MCP Platform | MCP 服务器名称 |
| `rate-limit.default-limit` | 100 | 默认限流次数 |
| `rate-limit.default-window` | 60 | 默认限流窗口 (秒) |

### 环境变量

生产环境建议使用环境变量配置：

```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_USERNAME=root
export DB_PASSWORD=your_password
export REDIS_HOST=localhost
export REDIS_PORT=6379
export JWT_SECRET=your-secret-key
```

## 📖 API 文档

### 服务管理 API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/admin/services` | 创建服务 |
| GET | `/api/admin/services` | 服务列表 |
| GET | `/api/admin/services/{id}` | 服务详情 |
| PUT | `/api/admin/services/{id}` | 更新服务 |
| DELETE | `/api/admin/services/{id}` | 删除服务 |
| POST | `/api/admin/services/{id}/enable` | 启用服务 |
| POST | `/api/admin/services/{id}/disable` | 禁用服务 |
| POST | `/api/admin/services/{id}/refresh` | 刷新工具 |

### API Key 管理 API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/admin/api-keys` | 创建 API Key |
| GET | `/api/admin/api-keys` | API Key 列表 |
| GET | `/api/admin/api-keys/{id}` | API Key 详情 |
| DELETE | `/api/admin/api-keys/{id}` | 删除 API Key |
| POST | `/api/admin/api-keys/{id}/enable` | 启用 API Key |
| POST | `/api/admin/api-keys/{id}/disable` | 禁用 API Key |

### 服务实例 API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/admin/instances` | 注册实例 |
| DELETE | `/api/admin/instances/{id}` | 注销实例 |
| GET | `/api/admin/instances/service/{serviceId}` | 实例列表 |
| PUT | `/api/admin/instances/{id}/health` | 更新健康状态 |

## 🔌 MCP 协议

### SSE 连接

MCP 协议通过 SSE (Server-Sent Events) 进行通信：

```bash
# 1. 建立 SSE 连接
curl -N http://localhost:8080/mcp/sse
```

响应示例：
```
event: endpoint
data: /mcp/message?sessionId=abc123
```

### 发送 MCP 消息

```bash
# 2. 发送 Initialize 请求
curl -X POST "http://localhost:8080/mcp/message?sessionId=abc123" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 1,
    "method": "initialize",
    "params": {
      "protocolVersion": "2024-11-05",
      "capabilities": {},
      "clientInfo": {"name": "test-client", "version": "1.0.0"}
    }
  }'
```

### 调用 Tools

```bash
# 获取 Tools 列表
curl -X POST "http://localhost:8080/mcp/message?sessionId=abc123" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 2,
    "method": "tools/list"
  }'

# 调用 Tool
curl -X POST "http://localhost:8080/mcp/message?sessionId=abc123" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 3,
    "method": "tools/call",
    "params": {
      "name": "get_users",
      "arguments": {"page": 1, "size": 10}
    }
  }'
```

## ❓ 常见问题

### 1. 启动时数据库连接失败

检查 MySQL 是否启动，确认用户名密码正确：
```bash
mysql -u root -p
```

### 2. Redis 连接失败

检查 Redis 是否启动：
```bash
redis-cli ping
# 应返回 PONG
```

### 3. OpenAPI 解析失败

确保 OpenAPI 规范格式正确，可以访问 [Swagger Editor](https://editor.swagger.io/) 验证。

### 4. MCP 工具调用失败

- 检查服务实例是否已注册且健康
- 检查 OpenAPI 规范是否正确解析
- 查看请求日志排查问题

## 📄 License

Copyright © 2024 MCP Platform

Released under the Apache License, Version 2.0.
