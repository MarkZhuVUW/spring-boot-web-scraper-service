#!/usr/bin/env bash
sed -i 's/\r$//'
set -e
clear

export TERM=${TERM:=xterm}

mvn -Plocal -f ../pom.xml clean install -DskipTests
cp ../webscraper-api/target/*-exec.jar ../api.jar

docker-compose rm -f
docker-compose up --build --force-recreate
