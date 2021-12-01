# temp container to build using gradle
FROM gradle:7.3.0-jdk17-alpine
WORKDIR /app
ADD --chown=gradle:gradle /app /app
RUN ./gradlew build --stacktrace

# actual container
FROM openjdk:13
COPY build/libs/*.jar .
CMD java -jar *.jar

