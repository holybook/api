# syntax=docker/dockerfile:1

# Build the React static bundle. Context is the repository root.
FROM node:20-alpine AS build
WORKDIR /web
COPY webclient/package.json webclient/package-lock.json ./
RUN npm ci
COPY webclient ./
RUN npm run build

# Caddy serves the static bundle and reverse-proxies /api to the Ktor server.
# The Caddyfile is baked in so the running container is self-contained.
FROM caddy:2-alpine
COPY dist/Caddyfile /etc/caddy/Caddyfile
COPY --from=build /web/build /srv
