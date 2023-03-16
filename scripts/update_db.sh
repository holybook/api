#!/bin/sh

DATA_DIR=$1

if [ -z "$DATA_DIR" ]
then
  echo "Please provide the data directory as first parameter"
  exit
fi

zip build/db.zip -r "$DATA_DIR"
scp build/db.zip ubuntu@holybook.app:~/

ssh ubuntu@holybook.app "rm -rf db"
ssh ubuntu@holybook.app "unzip db.zip -d db"
ssh ubuntu@holybook.app "java -jar dist/build/import-all.jar -i ~/db -u postgres"
ssh ubuntu@holybook.app "rm db.zip"



