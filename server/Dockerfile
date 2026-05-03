# ---------- STAGE 1: Build ----------
FROM gradle:8.7-jdk21 AS builder

WORKDIR /app

# Copy all files
COPY . .

# Build the project (skip tests for faster build)
RUN gradle clean build -x test

# ---------- STAGE 2: Run ----------
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copy jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose port (Render uses 8080 by default)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]