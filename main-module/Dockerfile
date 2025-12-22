FROM amazoncorretto:25
WORKDIR /app
COPY .env /app/.env
COPY main-module/target/main-module-1.0.jar /app/main-app.jar
CMD ["java", "-jar", "main-app.jar"]
