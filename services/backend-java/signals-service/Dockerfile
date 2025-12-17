# Hafnium Signals Service
FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -g 1000 hafnium && adduser -u 1000 -G hafnium -D -s /bin/false hafnium
WORKDIR /app
COPY --chown=hafnium:hafnium build/libs/*-SNAPSHOT.jar app.jar
USER hafnium
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
