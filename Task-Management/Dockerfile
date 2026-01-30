# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src ./src

# Install Maven and build
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/Task-Management-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Run application with dynamic port for Render
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]