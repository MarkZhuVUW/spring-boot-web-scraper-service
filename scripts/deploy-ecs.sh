#!/usr/bin/env bash
set -e
clear

export TERM=${TERM:=xterm}


aws configure set aws_access_key_id "$AWS_ACCESS_KEY_ID"
aws configure set aws_secret_access_key "$AWS_SECRET_ACCESS_KEY"


# shellcheck disable=SC2183
# shellcheck disable=SC2046
export $(printf "AWS_ACCESS_KEY_ID=%s AWS_SECRET_ACCESS_KEY=%s AWS_SESSION_TOKEN=%s" \
$(aws sts --debug assume-role \
--role-arn arn:aws:iam::142621353074:role/ECSS3UpdateRole \
--role-session-name assumeRolePracticeUser \
--query "Credentials.[AccessKeyId,SecretAccessKey,SessionToken]" \
--output text))

aws ecs update-service --profile update --cluster MarkZECSCluster --service WebscraperService --force-new-deployment --region ap-southeast-2