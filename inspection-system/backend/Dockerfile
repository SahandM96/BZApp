# Stage 1: Build the application
FROM maven:3.8.5-openjdk-17 AS builder

# Set the working directory
WORKDIR /app

# Copy the pom.xml first to leverage Docker cache for dependencies
COPY pom.xml .

# Download all dependencies. This layer is cached if pom.xml doesn't change.
# Using dependency:go-offline to download all plugins and dependencies
RUN mvn dependency:go-offline -B

# Copy the rest of the application source code
COPY src ./src

# Package the application (skip tests to speed up build in Docker, tests should be run in CI/CD pipeline)
# The JAR will be created in /app/target/
RUN mvn package -DskipTests -B

# Stage 2: Create the runtime image
# Use a lightweight JRE image for a smaller final image size
FROM openjdk:17-jre-slim

# Set the working directory
WORKDIR /app

# Create a non-root user and group
RUN groupadd -r appgroup && useradd --no-log-init -r -g appgroup appuser

# Copy the JAR file from the builder stage
# Make sure the artifactId in pom.xml matches 'inspection-backend'
COPY --from=builder /app/target/inspection-backend.jar ./app.jar

# Change ownership of the app directory and JAR file to the non-root user
RUN chown -R appuser:appgroup /app

# Switch to the non-root user
USER appuser

# Expose the port the application runs on (default for Spring Boot is 8080)
EXPOSE 8080

# Set the entrypoint to run the application
# Using exec form of ENTRYPOINT to make it the main process (PID 1)
ENTRYPOINT ["java", "-jar", "app.jar"]

# Optional: Add a healthcheck (Spring Boot Actuator needs to be an application dependency)
# HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
#   CMD curl -f http://localhost:8080/actuator/health || exit 1
