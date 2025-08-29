FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the JAR file into the container
COPY target/gateway-0.0.1-SNAPSHOT.jar app.jar

COPY src/main/resources/keys /app/keys

EXPOSE 8083

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]

