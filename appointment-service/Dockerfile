FROM openjdk:21-jdk-slim

WORKDIR /app

COPY build/libs/appointment-service-*.jar appointment-service.jar

EXPOSE 8089

ENTRYPOINT ["java", "-jar", "appointment-service.jar"]
