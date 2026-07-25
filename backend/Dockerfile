#FROM openjdk:11
FROM eclipse-temurin:11-jdk
ARG ACCESS_KEY
ARG SECRET_KEY

WORKDIR /app

COPY build/libs/SeeAndYouGoV2-0.0.1-SNAPSHOT.jar /app.jar
COPY src/main/java/com/SeeAndYouGo/SeeAndYouGo/restaurant/menuOfRestaurant1.json /app/src/main/java/com/SeeAndYouGo/SeeAndYouGo/restaurant/menuOfRestaurant1.json

EXPOSE 8080

ENV ACCESS_KEY $ACCESS_KEY
ENV SECRET_KEY $SECRET_KEY

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "/app.jar"]
