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

curl -d "{ \"ref\": \"$CIRCLE_BRANCH\", \"required_contexts\": [], \"description\": \"Automated deployment request from our pretty pipeline\", \"environment\": \"$ENVIRONMENT\", \"payload\": { \"version\": [1, 0, 0], \"team\": \"teamtag\", \"kubernetes\": {\"resources\": [ $(cat $NAISFIL)] } } }" -u $GITHUB_USERNAME:$GITHUB_ACCESS_TOKEN -X POST https://api.github.com/repos/$GITHUB_REPO/deployments
