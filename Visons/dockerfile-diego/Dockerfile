# Stage 1: Build with Maven
# Imagen oficial de Maven 3.9 con JDK 25 (Eclipse Temurin)
FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run with Java
# Imagen oficial de Eclipse Temurin JRE 25 (ligero, solo runtime)
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl
COPY --from=builder /app/target/*.jar app.jar
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
