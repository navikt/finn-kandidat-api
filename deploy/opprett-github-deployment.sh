#!/bin/bash

set -e

if [ $# -ne 2 ];
then
    >&2 echo "Bruk: $0 ENVIRONMENT NAIS-JSON-FIL"
    exit 1
fi

ENVIRONMENT=$1
NAISFIL=$2
GITHUB_REPO=$CIRCLE_PROJECT_USERNAME/$CIRCLE_PROJECT_REPONAME
REQUEST_HEADER="Accept: application/vnd.github.ant-man-preview+json"
REQUEST_DATA="{ \"ref\": \"$CIRCLE_BRANCH\", \"required_contexts\": [], \"description\": \"Automated deployment request from our pretty pipeline\", \"environment\": \"$ENVIRONMENT\", \"payload\": { \"version\": [1, 0, 0], \"team\": \"arbeidsgiver\", \"kubernetes\": {\"resources\": [ $(cat $NAISFIL)] } } }"
REQUEST_URL="https://api.github.com/repos/$GITHUB_REPO/deployments"
REQUEST_USER="$GITHUB_USERNAME:$GITHUB_ACCESS_TOKEN"

curl --verbose --fail --data "$REQUEST_DATA" --header "$REQUEST_HEADER" --user "$REQUEST_USER" $REQUEST_URL
