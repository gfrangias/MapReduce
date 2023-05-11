FROM openjdk:20

# Set the working directory
WORKDIR /app

# Copy the JAR file into the container
COPY monitor.jar /app/monitor.jar

# Expose the desired port
EXPOSE 7000

# Set the command to run the JAR file
CMD ["java", "-jar", "/app/monitor.jar"]