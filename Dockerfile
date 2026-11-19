# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre-jammy
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /build/target/authserver-0.0.1-SNAPSHOT.jar /app/app.jar
USER 10001:10001
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
