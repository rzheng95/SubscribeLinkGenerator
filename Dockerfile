# Use Amazon Corretto 17 as the base image
FROM amazoncorretto:17

# Specify the path to the JAR you want to use
COPY build/libs/SubscribeLinkGenerator-0.0.1-SNAPSHOT.jar app.jar

# Set the startup command to execute your JAR
ENTRYPOINT ["java", "-jar", "/app.jar"]
