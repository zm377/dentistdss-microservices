FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY ./target/admin-server-*.jar admin-server.jar

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "admin-server.jar"] 