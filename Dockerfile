FROM openjdk:21

# Set the working directory in the container
WORKDIR /app
ENV WORKDIR=/app

COPY gradlew    ./
COPY gradle     ./gradle
RUN ./gradlew --version

COPY build.gradle \
     settings.gradle \
     ./

COPY src/ ./src
# Copy the JAR file from the Gradle build directory to the container

RUN ./gradlew clean build --no-daemon
# Build the application
RUN ./gradlew build --no-daemon

COPY build/libs/*SNAPSHOT.jar app.jar

# Expose the port your Spring Boot app listens on (usually 8080)
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]