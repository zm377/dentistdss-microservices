FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY ./target/api-gateway-*.jar api-gateway.jar
COPY ./src/main/resources/certs /app/certs

EXPOSE 443

ENTRYPOINT ["java", "-jar", "api-gateway.jar"] 