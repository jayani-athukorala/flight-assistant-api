FROM maven:3.9.11-eclipse-temurin-25 AS build

WORKDIR /app

COPY pom.xml .

RUN mvn -B \
    -Dmaven.test.skip=true \
    -Dmaven.wagon.http.retryHandler.count=5 \
    dependency:go-offline

COPY src ./src

RUN mvn -B \
    -Dmaven.test.skip=true \
    -Dmaven.wagon.http.retryHandler.count=5 \
    clean package

FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]