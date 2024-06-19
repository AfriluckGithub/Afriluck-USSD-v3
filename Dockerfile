# Use a base image with Java runtime
FROM openjdk:11-jre-slim

LABEL maintainer="steven@afriluck.com"

VOLUME /tmp
# Set the working directory inside the container
#WORKDIR /app
EXPOSE 3005
# Copy the JAR file into the container
ARG JAR_FILE=target/afriluck-ussd-0.0.1-SNAPSHOT.jar

ADD ${JAR_FILE} app.jar

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]