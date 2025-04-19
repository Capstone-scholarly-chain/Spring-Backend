FROM openjdk:17-slim

WORKDIR /app

# JAR 파일 복사 (주석 해제)
COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
