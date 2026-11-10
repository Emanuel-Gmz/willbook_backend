# ───────────── Etapa 1: compilar con Maven ─────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package -DskipTests

# ───────────── Etapa 2: imagen liviana que solo corre el jar ─────────────
FROM eclipse-temurin:21-jre

ENV TZ=America/Argentina/Buenos_Aires
WORKDIR /app

RUN useradd --system --uid 10001 --home /app app && \
    mkdir -p uploads logs && chown app:app uploads logs

COPY --from=build --chown=app:app /app/target/willbook_backend-0.0.1-SNAPSHOT.jar app.jar
USER app

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]