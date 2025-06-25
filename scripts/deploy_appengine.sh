#!/bin/sh

set -e # Exit on errors

mkdir appengine-files

echo "Building server"
./gradlew :server:shadowJar # outputs server/build/libs/server-all.jar
cp server/build/libs/server-all.jar appengine-files

echo "Building web client"
( cd webclient ; npm run build ) # outputs webclient/build
cp -r webclient/build appengine-files/webclient

echo "Copy app.yaml"
cp app.yaml appengine-files/app.yaml

echo "Deploy to app engine"
( cd appengine-files ; gcloud app deploy )

rm -rf appengine-files