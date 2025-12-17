# Hafnium Identity Service
# Production Dockerfile

FROM eclipse-temurin:21-jre-alpine AS runtime

# Security: Create non-root user
RUN addgroup -g 1000 hafnium && \
    adduser -u 1000 -G hafnium -D -s /bin/false hafnium

WORKDIR /app

# Copy application JAR
COPY --chown=hafnium:hafnium build/libs/*-SNAPSHOT.jar app.jar

# Security: Run as non-root
USER hafnium

# Configure JVM for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
