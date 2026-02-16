FROM maven:3.9.4-eclipse-temurin-21-alpine
WORKDIR /app
COPY target/Hotel-booking-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
CMD ["java","-jar","app.jar"]
