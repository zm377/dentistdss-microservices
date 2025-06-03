FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY ./target/patient-service-*.jar patient-service.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "patient-service.jar"] 