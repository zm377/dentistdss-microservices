FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY ./target/user-profile-service-*.jar user-profile-service.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "user-profile-service.jar"]