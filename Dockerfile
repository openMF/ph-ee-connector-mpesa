FROM openjdk:13 AS builder
COPY . mpesa
WORKDIR /mpesa
RUN ./gradlew --no-daemon -q -x build
WORKDIR /mpesa/build/libs

FROM openjdk:13 as mpesa

COPY --from=builder /mpesa/build/libs/*.jar ph-ee-connector-mpesa-1.0.0-SNAPSHOT.jar

CMD java -jar *.jar

