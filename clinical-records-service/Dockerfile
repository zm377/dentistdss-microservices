FROM openjdk:21-jdk-slim

WORKDIR /app

COPY build/libs/clinical-records-service-*.jar app.jar

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar"]
