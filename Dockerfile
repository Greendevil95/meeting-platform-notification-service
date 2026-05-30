FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
COPY src ./src

RUN mvn -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN useradd --create-home --shell /usr/sbin/nologin appuser

COPY --from=build /workspace/target/*.jar app.jar

USER appuser

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
