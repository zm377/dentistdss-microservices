FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY ./target/system-service-*.jar system-service.jar

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "system-service.jar"] 