#!/usr/bin/env bash
set -e
clear

export ENVIRONMENT=${ENVIRONMENT:=prod}
export AWS_REGION=${AWS_REGION:=ap-southeast-2}

echo $ENVIRONMENT
echo $AWS_REGION

export TERM=${TERM:=xterm}
export DOCKERHUB_USERNAME=${DOCKERHUB_USERNAME:=zdy120939259}
export DOCKERHUB_PASSWORD=${DOCKERHUB_PASSWORD:=Mark19960630}
export GITHUB_USERNAME=${GITHUB_USERNAME:=MarkZhuVUW}

echo $DOCKERHUB_PASSWORD | docker login -u $DOCKERHUB_USERNAME --password-stdin


docker-compose build
docker-compose push

