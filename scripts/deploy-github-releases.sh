#!/usr/bin/env bash
set -e
clear

export TERM=${TERM:=xterm}
export VERSION=${VERSION:=0.0.1}
echo $VERSION;

go get github.com/tcnksm/ghr
mkdir artifacts
cp ../api.jar ./artifacts
cp ../client.jar ./artifacts
ghr -t "${GITHUB_TOKEN}" -u "${CIRCLE_PROJECT_USERNAME}" -r "${CIRCLE_PROJECT_REPONAME}" -c "${CIRCLE_SHA1}" -delete ${VERSION} ./artifacts/
