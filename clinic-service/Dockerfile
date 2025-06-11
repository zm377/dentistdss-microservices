FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY ./build/libs/clinic-service-*.jar clinic-service.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "clinic-service.jar"] 