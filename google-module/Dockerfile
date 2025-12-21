FROM amazoncorretto:25
WORKDIR /app
COPY .env /app/.env
COPY google_credentials.json /app/google_credentials.json
COPY google-module/target/google-module-1.0-SNAPSHOT.jar /app/google-module.jar
CMD ["java", "-jar", "google-module.jar"]
