## 问题分析

根据 Railway 部署日志，错误信息为：
```
ERROR: failed to build: failed to solve: openjdk:21-jre-slim: failed to resolve source metadata for docker.io/library/openjdk:21-jre-slim: docker.io/library/openjdk:21-jre-slim: not found
```

这表明 `openjdk:21-jre-slim` 镜像在 Docker Hub 上不存在或无法访问。

## 解决方案

修改 Dockerfile，使用多阶段构建：

1. **第一阶段**：使用 Maven 镜像构建项目
   - 使用官方 Maven 镜像
   - 运行 `mvn clean package -DskipTests` 构建 jar 包

2. **第二阶段**：使用轻量级 Java 运行时镜像
   - 使用 `eclipse-temurin:21-jre-alpine` 镜像（官方支持的 OpenJDK 镜像）
   - 从第一阶段复制构建好的 jar 包
   - 设置启动命令

## 具体修改

修改 `Dockerfile` 文件，将当前的单阶段构建改为多阶段构建，确保使用可用的 Java 镜像。