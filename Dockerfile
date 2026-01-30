# Step 1: Use official Java image
FROM openjdk:20-jdk-slim


# Step 2: Set working directory inside container
WORKDIR /app

# Step 3: Copy the built jar file into container
COPY Task-Management/target/Task-Management-0.0.1-SNAPSHOT.jar app.jar

# Step 4: Expose port (Render will override via PORT variable)
EXPOSE 8080

# Step 5: Run the jar with dynamic port
CMD ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]
