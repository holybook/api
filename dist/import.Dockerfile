# syntax=docker/dockerfile:1

# Build the import CLI distribution. Context is the repository root so that
# buildSrc and the :lib module the importer depends on are available.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY . .
RUN ./gradlew --no-daemon :import:installDist

# Slim runtime with the generated launcher script + dependency jars.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/import/build/install/import ./import
# Args (e.g. -jdbc <url> -i /content) are appended by `docker compose run`.
ENTRYPOINT ["/app/import/bin/import"]
