#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
pushd $DIR >/dev/null
docker-compose -p wenet_task_manager_services_dev -f src/dev/docker/docker-compose.yml down --remove-orphans
docker stop wenet_task_manager_dev
docker rm wenet_task_manager_dev
popd >/dev/null