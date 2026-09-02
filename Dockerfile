# ============================================================
# BUILD STAGE
# ============================================================
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /workspace

# Copy pom.xml first to improve dependency caching.
COPY pom.xml ./

# Download project dependencies.
RUN mvn -q -DskipTests dependency:go-offline

# Copy application source.
COPY src ./src

# Build the executable Spring Boot JAR.
RUN mvn -q -DskipTests clean package


# ============================================================
# RUNTIME STAGE
# ============================================================
FROM eclipse-temurin:25-jre

WORKDIR /app

# Run the application as a non-root user.
RUN groupadd --system spring && \
    useradd --system --gid spring spring

# Copy the generated JAR from the build stage.
COPY --from=build --chown=spring:spring \
    /workspace/target/*.jar \
    /app/app.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]