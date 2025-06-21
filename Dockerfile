FROM eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Copy the prebuilt JAR
COPY target/myspace-0.0.1-SNAPSHOT.jar app.jar

# Expose port (must match your Spring Boot server.port, default is 8080)
EXPOSE 8080

# Set JVM options (limit memory to avoid crashes on free-tier hosts)
ENTRYPOINT ["java", "-Xms128m", "-Xmx512m", "-jar", "app.jar"]
