# Use a base image with Java runtime
FROM openjdk:21-bullseye

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file into the container
COPY target/afriluck-ussd-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your Spring Boot app runs on
EXPOSE 5000

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]