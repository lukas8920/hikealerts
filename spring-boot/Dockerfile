# Use an official OpenJDK 17 image as the base
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the application jar to the working directory
COPY /target/microsoft-hackathon-1.0.0.jar /app/hiking-alerts.jar

# Expose the application's port (adjust if your app uses a different port)
EXPOSE 8080

# Define the command to run the application
ENTRYPOINT ["java", "-Xmx8g", "-Xms512m", "-jar", "/app/hiking-alerts.jar"]
