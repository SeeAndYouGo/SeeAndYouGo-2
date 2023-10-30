# # Base image
# FROM openjdk:11 as bulider
# WORKDIR /app
# COPY . .
# CMD ['./gradlew', 'assemble']


# FROM openjdk:11
# #WORKDIR /app

# # Copy JAR file to container
# COPY build/libs/SeeAndYouGoV2-0.0.1-SNAPSHOT.jar seeandyougoV2-0.0.1-SNAPSHOT.jar

# EXPOSE 8080

# # Entry point for running the application
# CMD ["java", "-jar", "seeandyougoV2-0.0.1-SNAPSHOT.jar"]
FROM openjdk:11
ARG ACCESS_KEY
ARG SECRET_KEY


WORKDIR /app
COPY gradlew .
COPY gradle gradle 
COPY build.gradle .
COPY settings.gradle .
COPY src src
RUN chmod +x ./gradlew
RUN ./gradlew bootJar



#COPY build/libs/*.jar app.jar

EXPOSE 8080

ENV ACCESS_KEY ${ACCESS_KEY}
ENV SECRET_KEY ${SECRET_KEY}
ENTRYPOINT ["java","-Daccess_key=\"$ACCESS_KE\"","-Dsecret_key=\"$SECRET_KEY\"","-jar","build/libs/SeeAndYouGoV2-0.0.1-SNAPSHOT.jar"]