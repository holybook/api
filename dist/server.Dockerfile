# syntax=docker/dockerfile:1

# Build the Ktor fat jar. Context is the repository root so that buildSrc and
# the :lib module the server depends on are available.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY . .
RUN ./gradlew --no-daemon :server:shadowJar

# Slim runtime image with just a JRE and the fat jar.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/server/build/libs/server-all.jar ./holybook.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/holybook.jar"]
