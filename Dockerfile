# 多阶段构建
# 第一阶段：构建项目
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 第二阶段：运行项目
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/stock-and-fund-*.jar /app/stock-and-fund.jar
RUN touch /app/stock-and-fund.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","-Duser.timezone=GMT+08","-Xmx256m","-Xms256m","/app/stock-and-fund.jar"]