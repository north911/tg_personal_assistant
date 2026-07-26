# syntax=docker/dockerfile:1

# Runtime-only image. Build the jar first on the host:  ./mvnw clean package
# Build context is the project root (see docker-compose.yml: build.context = ..).
FROM eclipse-temurin:25-jre
WORKDIR /app

# The SQLite DB lives under /app/db, mounted as a volume so data persists.
RUN mkdir -p /app/db

COPY target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
