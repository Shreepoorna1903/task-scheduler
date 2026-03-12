FROM eclipse-temurin:11-jre

WORKDIR /app

COPY target/task-scheduler-1.0-SNAPSHOT.jar app.jar
COPY config.yml config.yml

EXPOSE 8080 8081

CMD ["java", "-jar", "app.jar", "server", "config.yml"]