#!/bin/bash
if [ -f /.dockerenv ]; then
   echo "You can not start the development environment inside a docker container"
else
	DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
	pushd $DIR >/dev/null
	docker-compose -p wenet_task_manager_services_dev -f src/dev/docker/docker-compose.yml up --remove-orphans -d
	DOCKER_BUILDKIT=1 docker build -f src/dev/docker/Dockerfile -t internetofus/task-manager:dev .
	docker run --name wenet_task_manager_dev -v /var/run/docker.sock:/var/run/docker.sock -v ${HOME}/.m2/repository:/root/.m2/repository  -v ${PWD}:/app -p 5005:5005 -it internetofus/task-manager:dev /bin/bash
	popd >/dev/null
fi