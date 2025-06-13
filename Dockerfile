FROM gradle:8.6.0-jdk21 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew build
FROM openjdk:21-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]