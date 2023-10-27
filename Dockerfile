FROM adoptopenjdk/openjdk11:latest

ENV APP_HOME /app
ENV JAR_FILE web-crawler-0.0.1-SNAPSHOT.jar

# Create the application directory
RUN mkdir -p $APP_HOME

# Set the working directory
WORKDIR $APP_HOME

# Copy the executable JAR file into the container at /app
COPY /build/libs/$JAR_FILE $APP_HOME/$JAR_FILE

# Expose the port your application runs on
EXPOSE 8080

# Define the command to run your application
CMD ["java", "-jar", "/app/web-crawler-0.0.1-SNAPSHOT.jar"]