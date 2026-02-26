# 使用预构建的 JAR 包
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/stock-and-fund-*.jar /app/stock-and-fund.jar
RUN touch /app/stock-and-fund.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","-Duser.timezone=GMT+08","-Xmx256m","-Xms256m","/app/stock-and-fund.jar"]