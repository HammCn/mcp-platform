#!/bin/sh

# ----------------------------------------------------------------------------
# Maven Wrapper Script
# Downloads and uses Maven from the wrapper
# ----------------------------------------------------------------------------

MAVEN_VERSION=3.9.6
MAVEN_HOME="$HOME/.m2/wrapper/maven-$MAVEN_VERSION"
MAVEN_URL="https://dlcdn.apache.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.zip"

# 检查是否已下载
if [ ! -d "$MAVEN_HOME" ]; then
    echo "Downloading Maven $MAVEN_VERSION..."
    mkdir -p "$HOME/.m2/wrapper"
    cd "$HOME/.m2/wrapper"
    
    # 使用 curl 下载
    curl -L -o maven.zip "$MAVEN_URL"
    unzip -q maven.zip
    rm maven.zip
    
    MAVEN_HOME="$HOME/.m2/wrapper/apache-maven-$MAVEN_VERSION"
    echo "Maven downloaded to: $MAVEN_HOME"
fi

# 执行 Maven
exec "$MAVEN_HOME/bin/mvn" "$@"
