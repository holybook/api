#!/bin/sh

scripts/build.sh

./gradlew :import:buildFatJar

mkdir dist/build
cp server/build/libs/server-all.jar dist/build
cp import/build/libs/import-all.jar dist/build

cp -r raw dist/

zip build/dist.zip -r dist
scp build/dist.zip ubuntu@holybook.app:~/