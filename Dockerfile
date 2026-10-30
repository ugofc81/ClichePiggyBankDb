# Stage 1: Build the JAR
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew bootJar -x test

# Stage 2: Create a lightweight runtime environment
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 9092
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=9092"]