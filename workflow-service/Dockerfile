FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY ./build/libs/workflow-service-*.jar workflow-service.jar

EXPOSE 8093

ENTRYPOINT ["java", "-jar", "workflow-service.jar"]
