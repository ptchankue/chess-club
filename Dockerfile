# Multi-stage build for Spring Boot Chess Club Application

# Stage 1: Build the application
FROM maven:3.9.11-eclipse-temurin-21 AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml first for better layer caching
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Install necessary packages and create a non-root user for security
RUN apk add --no-cache curl && \
    addgroup -S chessclub && \
    adduser -S -G chessclub chessclub

# Copy the built JAR from builder stage
COPY --from=builder /app/target/chessclub-*.jar app.jar

# Set ownership to non-root user
RUN chown -R chessclub:chessclub /app

# Switch to non-root user
USER chessclub

# Expose the application port
EXPOSE 8084

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8084/actuator/health || exit 1

# Set JVM options for production
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
