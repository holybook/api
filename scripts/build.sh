#!/bin/sh

(cd webclient && npm run build)
rm -r server/src/main/resources/client
mkdir -p server/src/main/resources/client
cp -r webclient/build/* server/src/main/resources/client/
./gradlew :server:buildFatJar