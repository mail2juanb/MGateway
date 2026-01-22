# Use a lightweight Alpine image with JRE 21 (no need for JDK in production) or eclipse-temurin:21-jdk-jammy (quite heavy but with a JDK)
FROM eclipse-temurin:21-jre-alpine

# Sets the working directory in the container
WORKDIR /app

# Environment variables for configuration
ENV EUREKA_SERVER_HOST=eureka-server
ENV EUREKA_SERVER_PORT=9102
ENV HOSTNAME=mgateway

# Copy the application jar file
COPY target/mgateway-*.jar app.jar

# Expose the port listening
EXPOSE 9010

# Command to launch the application
ENTRYPOINT ["java", "-jar", "app.jar"]