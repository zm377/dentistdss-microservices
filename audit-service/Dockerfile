FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY ./build/libs/audit-service-*.jar audit-service.jar

EXPOSE 8087

ENTRYPOINT ["java", "-jar", "audit-service.jar"] 