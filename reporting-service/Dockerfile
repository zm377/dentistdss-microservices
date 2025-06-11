# Multi-stage build for Reporting Service
FROM openjdk:21-jdk-slim as builder

WORKDIR /app
COPY build.gradle .
COPY src src
COPY gradle gradle
COPY gradlew .

# Build the application
RUN ./gradlew bootJar --no-daemon

# Runtime stage
FROM openjdk:21-jre-slim

WORKDIR /app

# Install required packages for PDF generation and fonts
RUN apt-get update && apt-get install -y \
    fontconfig \
    fonts-dejavu-core \
    fonts-liberation \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -r reporting && useradd -r -g reporting reporting

# Create directories for temp files and reports
RUN mkdir -p /app/temp /app/reports && \
    chown -R reporting:reporting /app

# Copy the built JAR
COPY --from=builder /app/build/libs/*.jar app.jar

# Change ownership to non-root user
RUN chown reporting:reporting app.jar

USER reporting

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8092/reports/health || exit 1

EXPOSE 8092

# JVM optimization for containerized environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
