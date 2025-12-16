# syntax=docker/dockerfile:1

# Hafnium Backend Service Dockerfile
# Multi-stage build for production-ready container

# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy Gradle files for dependency caching
COPY gradle gradle
COPY gradlew build.gradle settings.gradle gradle.properties ./
COPY common common
COPY identity-service identity-service

# Download dependencies
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon

# Build the service
ARG SERVICE_NAME=identity-service
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :${SERVICE_NAME}:bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Security: run as non-root user
RUN addgroup -g 1001 hafnium && \
    adduser -u 1001 -G hafnium -D hafnium

WORKDIR /app

# Copy built artifact
ARG SERVICE_NAME=identity-service
COPY --from=builder /build/${SERVICE_NAME}/build/libs/${SERVICE_NAME}.jar app.jar

# Set ownership
RUN chown -R hafnium:hafnium /app

USER hafnium

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health/readiness || exit 1

# Expose port
EXPOSE 8080

# JVM configuration for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
