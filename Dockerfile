FROM openjdk:8-jdk-alpine

COPY target/kafka-wordsnake-0.0.1-SNAPSHOT.jar app.jar
#RUN mkdir /tmp/wordsnakes | touch /tmp/wordsnakes/wordsnakes.txt

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
