FROM maven:3.8.4-jdk-11-slim as builder

COPY common /usr/src/app/common
COPY spring-boot-dixit /usr/src/app/spring-boot-dixit
COPY dixit /usr/src/app/dixit
COPY pom.xml /usr/src/app

RUN mvn -f /usr/src/app/pom.xml clean package

FROM adoptopenjdk/openjdk11:alpine-slim
RUN apk add build-base
COPY --from=builder /usr/src/app/spring-boot-dixit/target/*.jar /app.jar
EXPOSE 80
CMD /opt/java/openjdk/bin/java -jar /app.jar