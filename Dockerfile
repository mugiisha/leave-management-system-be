# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the Maven/Gradle build file and dependencies
COPY target/leavemanagementsystem-0.0.1-SNAPSHOT.jar app.jar

# Copy the .env file into the container
COPY .env .env

# Expose the port the app runs on
EXPOSE 8080

# Set environment variables from the .env file
# This requires the `envsubst` tool, which is part of the gettext package
RUN apt-get update && apt-get install -y gettext-base && rm -rf /var/lib/apt/lists/*

# Replace environment variables in the application
RUN envsubst < .env > /app/.env

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]