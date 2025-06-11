FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY ./build/libs/notification-service-*.jar notification-service.jar

EXPOSE 8088

ENTRYPOINT ["java", "-jar", "notification-service.jar"] 