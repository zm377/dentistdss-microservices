FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY ./build/libs/clinic-admin-service-*.jar clinic-admin-service.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "clinic-admin-service.jar"]