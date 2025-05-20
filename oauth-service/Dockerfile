FROM eclipse-temurin:21-jdk

WORKDIR /app

# The JAR filename pattern should match what Maven produces in oauth-service/target/
COPY ./target/oauth-service-*.jar oauth-service.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "oauth-service.jar"] 