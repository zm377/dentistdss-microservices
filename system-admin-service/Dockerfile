FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY ./build/libs/system-service-*.jar system-service.jar

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "system-service.jar"] 