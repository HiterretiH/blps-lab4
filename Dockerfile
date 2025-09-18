FROM openjdk:23
COPY target/blps-lab1-1.0.jar /app/blps-lab1-1.0.jar
WORKDIR /app
CMD ["java", "-jar", "blps-lab1-1.0.jar"]
