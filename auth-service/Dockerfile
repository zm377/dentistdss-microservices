FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY ./target/auth-service-*.jar auth-service.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "auth-service.jar"] 