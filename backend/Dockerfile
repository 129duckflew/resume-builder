# 构建阶段固定跑在构建机原生架构（Java 字节码与架构无关），
# 避免多架构构建时 Maven 在 QEMU 模拟下运行。
FROM --platform=$BUILDPLATFORM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

FROM mcr.microsoft.com/playwright/java:v1.61.0
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
