#!/bin/bash

# =====================================================
# MCP Platform 编译脚本
# 使用 IDEA 内置 Maven
# =====================================================

IDEA_MVN="/Users/hamm/Applications/IntelliJ IDEA Ultimate.app/Contents/plugins/maven/lib/maven3/bin/mvn"

if [ ! -x "$IDEA_MVN" ]; then
    echo "❌ 错误：找不到 IDEA Maven"
    exit 1
fi

cd /Users/hamm/Desktop/mcp-platform

echo "============================================="
echo "   使用 IDEA Maven 编译项目..."
echo "============================================="

# 编译项目
"$IDEA_MVN" clean install -DskipTests "$@"
