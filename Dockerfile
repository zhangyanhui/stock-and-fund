# 多阶段构建
# 第一阶段：构建项目
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# 配置国内 Maven 镜像源
RUN mkdir -p /root/.m2 && \
    cat > /root/.m2/settings.xml << 'EOF'
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <mirrors>
    <mirror>
      <id>aliyunmaven</id>
      <mirrorOf>*</mirrorOf>
      <name>阿里云公共仓库</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
</settings>
EOF

COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 第二阶段：运行项目
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/stock-and-fund-*.jar /app/stock-and-fund.jar
RUN touch /app/stock-and-fund.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","-Duser.timezone=GMT+08","-Xmx256m","-Xms256m","/app/stock-and-fund.jar"]