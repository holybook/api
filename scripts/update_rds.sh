#!/bin/zsh

RDS_HOSTNAME="awseb-e-w65tw3mhvk-stack-awsebrdsdatabase-t7akb0ich3b0.crzc87t7qore.eu-central-1.rds.amazonaws.com"
RDS_USERNAME=postgres
RDS_DB_NAME=ebdb
RDS_PORT=5432

./gradlew :import:installDist
import/build/install/import/bin/import \
  -h $RDS_HOSTNAME \
  -p $RDS_PORT \
  -u $RDS_USERNAME \
  -d $RDS_DB_NAME \
  -pwd