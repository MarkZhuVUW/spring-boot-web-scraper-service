#!/usr/bin/env bash
set -e
clear

export ENVIRONMENT=${ENVIRONMENT:=prod}
export AWS_REGION=${AWS_REGION:=ap-southeast-2}


export TERM=${TERM:=xterm}

echo $ENVIRONMENT;
echo $AWS_REGION;
case "$ENVIRONMENT" in
  local)
    mvn -Plocal -f ../pom.xml clean install -DskipTests
    ;;
  test)
    echo not implemented!
    ;;
  prod)
    export VERSION=${VERSION:=0.0.1} # Update release version so that Docker Hub and GitHub can have the correct release version
    export DOCKERHUB_USERNAME=${DOCKERHUB_USERNAME:=zdy120939259}
    export DOCKERHUB_PASSWORD=${DOCKERHUB_PASSWORD:=Mark19960630}
    export GITHUB_USERNAME=${GITHUB_USERNAME:=MarkZhuVUW}
    echo $DOCKERHUB_PASSWORD | docker login -u $DOCKERHUB_USERNAME --password-stdin
    docker-compose build
    docker-compose push
    ;;
esac