#!/bin/bash

# =====================================================
# MCP Platform 快速启动脚本
# =====================================================

echo "============================================="
echo "   MCP Platform - 快速启动脚本"
echo "============================================="
echo ""

# 检查 Java
if ! command -v java &> /dev/null; then
    echo "❌ 错误：未找到 Java，请安装 JDK 17+"
    exit 1
fi
java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
echo "✓ Java 版本：$(java -version 2>&1 | head -n 1)"

# 检查 Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ 错误：未找到 Maven，请安装 Maven 3.8+"
    echo ""
    echo "   macOS 安装命令：brew install maven"
    echo "   或者使用 Maven Wrapper: ./mvnw"
    exit 1
fi
echo "✓ Maven 版本：$(mvn --version | head -n 1)"

# 检查 MySQL
echo ""
echo "正在检查 MySQL 连接..."
if ! command -v mysql &> /dev/null; then
    echo "⚠️  MySQL 客户端未安装，请手动初始化数据库"
    echo ""
    echo "   数据库初始化 SQL: database/schema.sql"
    echo "   数据库地址：localhost:3306"
    echo "   数据库名称：mcp_platform"
    echo "   用户名：root"
    echo "   密码：asdfew12345"
else
    # 尝试连接 MySQL
    if mysql -u root -pasdfew12345 -e "USE mcp_platform" 2>/dev/null; then
        echo "✓ MySQL 连接成功，数据库 mcp_platform 已存在"
    else
        echo "⚠️  数据库 mcp_platform 不存在或连接失败"
        echo "   请先执行数据库初始化："
        echo "   mysql -u root -pasdfew12345 < database/schema.sql"
    fi
fi

# 检查 Redis
echo ""
echo "正在检查 Redis 连接..."
if command -v redis-cli &> /dev/null; then
    if redis-cli ping &> /dev/null; then
        echo "✓ Redis 连接成功"
    else
        echo "⚠️  Redis 未启动，请先启动 Redis"
        echo "   启动命令：redis-server"
    fi
else
    echo "⚠️  Redis 客户端未安装"
fi

# 编译项目
echo ""
echo "============================================="
echo "   开始编译项目..."
echo "============================================="
mvn clean install -DskipTests

if [ $? -ne 0 ]; then
    echo ""
    echo "❌ 编译失败，请检查错误信息"
    exit 1
fi

echo ""
echo "✓ 编译成功!"

# 启动应用
echo ""
echo "============================================="
echo "   启动 MCP Platform..."
echo "============================================="
echo ""
mvn spring-boot:run -pl mcp-admin
