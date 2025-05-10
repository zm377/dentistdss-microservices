FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY ./target/oauth-service-*.jar oauth-service.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "oauth-service.jar"] 