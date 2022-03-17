#!/bin/bash
if ! docker stats --no-stream >/dev/null 2>&1; then
    echo "Docker does not seem to be running, run it first and retry"
    RESULT=1
else
	DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
	pushd "$DIR" >/dev/null
	COMPONENT_VERSION=$(grep --max-count=1 '<version>' pom.xml | awk -F '>' '{ print $2 }' | awk -F '<' '{ print $1 }')
	COMPONENT_NAME="internetofus/task-manager"
	DOCKER_TAG="$COMPONENT_NAME:$COMPONENT_VERSION"
	DOCKER_ARGS=""
	if [ "no-cache" = "$1" ];
	then
		PROFILE=${2:-"gitlab"}
		DOCKER_ARGS="$DOCKER_ARGS --no-cache"
	else
		PROFILE=${1:-"gitlab"}
	fi
	DOCKER_ARGS="$DOCKER_ARGS --build-arg DEFAULT_PROFILE=$PROFILE"
	DOCKER_BUILDKIT=1 docker build $DOCKER_ARGS -f src/main/docker/Dockerfile -t $DOCKER_TAG .
	RESULT=$?
	popd >/dev/null
fi
exit $RESULT