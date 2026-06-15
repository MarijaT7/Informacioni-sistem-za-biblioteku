# syntax=docker/dockerfile:1.4
# ── Stage 1: build ───────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# cache dependencies before copying source
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2/repository mvn dependency:go-offline -q

# copy source and build — skip tests (ES not available at build time)
COPY src ./src
RUN --mount=type=cache,target=/root/.m2/repository mvn package -DskipTests -q

# ── Stage 2: runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends tesseract-ocr tesseract-ocr-all \
    && rm -rf /var/lib/apt/lists/*

# non-root user
RUN groupadd -r spring && useradd -r -g spring spring
USER spring

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
