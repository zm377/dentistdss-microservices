FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY build/libs/genai-service-*.jar genai-service.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "genai-service.jar"]