# 使用Maven基础镜像进行构建
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# 设置工作目录
WORKDIR /app

# 复制项目文件
COPY pom.xml .
COPY src ./src

# 运行Maven构建
RUN mvn clean package -DskipTests

# 使用JRE运行时镜像
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 从构建阶段复制JAR文件
COPY --from=builder /app/target/stock-and-fund-*.jar /app/stock-and-fund.jar

# 触发JAR文件
RUN touch /app/stock-and-fund.jar

# 设置入口点
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","-Duser.timezone=GMT+08","-Xmx256m","-Xms256m","/app/stock-and-fund.jar"]