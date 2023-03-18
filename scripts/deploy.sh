#!/bin/sh

./gradlew :import:buildFatJar
./gradlew :server:buildFatJar

mkdir dist/build
cp server/build/libs/server-all.jar dist/build
cp import/build/libs/import-all.jar dist/build

mkdir dist/client
(cd webclient && npm run build)
cp -r webclient/build/* dist/client/

zip build/dist.zip -r dist
scp build/dist.zip ubuntu@holybook.app:~/

ssh ubuntu@holybook.app 'sudo mkdir -p /var/server/data'
ssh ubuntu@holybook.app 'sudo rm -rf dist'
ssh ubuntu@holybook.app 'unzip dist.zip'
ssh ubuntu@holybook.app 'rm dist.zip'

ssh ubuntu@holybook.app '(cd dist && sudo docker compose down)'
ssh ubuntu@holybook.app '(cd dist && sudo docker compose up --build -d)'