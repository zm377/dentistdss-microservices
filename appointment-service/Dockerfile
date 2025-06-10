FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/appointment-service-*.jar app.jar

EXPOSE 8089

ENTRYPOINT ["java", "-jar", "appointment-service.jar"]
