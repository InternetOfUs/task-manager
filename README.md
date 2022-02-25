# WeNet - Task manager

## Introduction

The task manager component is the one responsible for storing and maintaining
the task and task types. The task types define the interaction protocols between users
and the task is an instance of this type.

The task type uses the [OpenAPI](https://swagger.io/specification/) to define
the possible attributes of a task, the actions or transactions that a user can do
in a task, and the messages or callbacks that can be posted to the application
from the task execution. Also, it has a set of norms that describe the behaviour
of the user on the task execution.

When a set of users want to be coordinated to do something one of them create a new task.
This task has associated a type that describes the behaviour of the user on this execution.
The creator of the task, if wants, can define new norms that can modify this default
behaviour. The task manager is also responsible for maintaining the state of this task execution.
For this reason on the task model are stored all the transactions that have been done
on it, and on this transaction are stored the messages that has been sent to the application
when this transaction is executed. As well, it provides services that can be used
to obtain the messages sent to the users by the application callbacks.


## Setup and configuration

First of all, you must install the next software.

 - [docker](https://docs.docker.com/install/)
 - [docker compose](https://docs.docker.com/compose/install/)

### Requirements

The profile manager component requires:

 - [MongoDB](https://docs.mongodb.com/manual/installation/)
 - [Profile manager](https://github.com/InternetOfUs/profile-manager)
 - [Interaction protocol engine](https://github.com/InternetOfUs/interaction-protocol-engine/)
 - [Service API](https://github.com/InternetOfUs/service-api/)

### Development

The development is done using a docker image that can be created and started with the script `./startDevelopmentEnvironment.sh`.
The scrip start the next services:

 - [Mongo express](http://localhost:8081)
 - [Swagger editor](http://localhost:8080)

And also start a bash console where you can compile and test the project. The project uses the [Apache maven](https://maven.apache.org/)
to solve the dependencies, generate the Open API documentation, compile the component and run the test.

 - Use `mvn dependency:list` to show the component dependencies.
 - Use `mvn compile` to compile and generate the Open API documentation (**target/classes/wenet-task_manager-openapi.yml**).
 - Use `mvn test` to run the test.
 - Use `mvn -Dmaven.surefire.debug="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=0.0.0.0:5005 -Xnoagent -Djava.compiler=NONE" test` to run the test on debug mode.
 - Use `mvn site` to generate a HTML page (**target/site/index.html**) with all the reports (test, javadoc, PMD,CPD and coverage).

Finally, you can stop the development exiting the bash and executing the script `./stopDevelopmentEnvironment.sh`.


### Create docker image

If you want to create an image execute the next command.

```
./buildDockerImage.sh
```

This creates the generic docker image, but you can create a different wit the **docker build** command and using the next arguments:

 - **DEFAULT_API_HOST** to define the host where the API has to bind. By default is **0.0.0.0**.
 - **DEFAULT_API_PORT** to define the port where the API has to bind. By default is **8080**.
 - **DEFAULT_DB_HOST** to define the mongo database server hostname. By default is **localhost**.
 - **DEFAULT_DB_PORT** to define the mongo database server port. By default is **27017**.
 - **DEFAULT_DB_NAME** to define the mongo database name. By default is **wenetTaskManagerDB**.
 - **DEFAULT_DB_USER_NAME** to define the mongo database user name. By default is **wenetTaskManager**.
 - **DEFAULT_DB_USER_PASSWORD** to define the mongo database user password. By default is **password**.
 - **DEFAULT_WENET_PROFILE_MANAGER_API** to define the path to the profile manager component to use. By default is **https://wenet.u-hopper.com/prod/profile_manager**.
 - **DEFAULT_WENET_SERVICE_API** to define the path to the service component to use. By default is **https://wenet.u-hopper.com/prod/service**.
 - **DEFAULT_WENET_INTERACTION_PROTOCOL_ENGINE_API** to define the path to the interaction protocol engine component to use. By default is **https://wenet.u-hopper.com/prod/interaction_protocol_engine**.
 - **DEFAULT_CACHE_TIMEOUT** to define the time in seconds that a value can be on the cache. By default is **300**.
 - **DEFAULT_CACHE_SIZE** to define the maximum number of entries that can be on the cache. By default is **10000**.

Also, you can define your configuration that modifies these properties and mount to  **/usr/wenet/task-manager/etc**.


### Run, configure and link with a MongoDB

You can start this component starting the [latest docker image upload to docker hub](https://hub.docker.com/r/internetofus/task-manager).

```
docker run internetofus/task-manager:latest
```

On this container, you can use the next environment variables:

 - **API_HOST** to define the host where the API has to bind. By default is **0.0.0.0**.
 - **API_PORT** to define the port where the API has to bind. By default is **8080**.
 - **DB_HOST** to define the mongo database server hostname. By default is **localhost**.
 - **DB_PORT** to define the mongo database server port. By default is **27017**.
 - **DB_NAME** to define the mongo database name. By default is **wenetTaskManagerDB**.
 - **DB_USER_NAME** to define the mongo database user name. By default is **wenetTaskManager**.
 - **DB_USER_PASSWORD** to define the mongo database user password. By default is **password**.
 - **WENET_PROFILE_MANAGER_API** to define the path to the profile manager component to use. By default is **https://wenet.u-hopper.com/prod/profile_manager**.
 - **WENET_SERVICE_API** to define the path to the service component to use. By default is **https://wenet.u-hopper.com/prod/service**.
 - **WENET_INTERACTION_PROTOCOL_ENGINE_API** to define the path to the interaction protocol engine component to use. By default is **https://wenet.u-hopper.com/prod/interaction_protocol_engine**.
 - **COMP_AUTH_KEY** to define the authentication key that the component has to use to interact with the other WeNet components.
 - **CACHE_TIMEOUT** to define the time in seconds that a value can be on the cache. By default is **300**.
 - **CACHE_SIZE** to define the maximum number of entries that can be on the cache. By default is **10000**.

When the container is started, it stores the log messages at **/usr/wenet/task-manager/var/log/task-manager.log**. This file is limited
to 10 MB and rolled every day using the pattern **task-manager.log.X** (where X is a number between 1 and 99).

If you want to start also a database and link both you can use the defined docker compose configuration.

```
docker-compose -f src/main/docker/docker-compose.yml up -d
```

This docker compose has the next variables:

 - **TASK_MANAGER_API_PORT** to define the port to listen for the API calls. By default is **8083**.
 - **MONGO_ROOT_USER** to define the root user for the MongoDB. By default is **root**.
 - **MONGO_ROOT_PASSWORD** to define the password of the root user for the MongoDB. By default is **password**.
 - **WENET_PROFILE_MANAGER_API** to define the path to the task manager component to use. By default is **https://wenet.u-hopper.com/prod/profile_manager**.
 - **WENET_SERVICE_API** to define the path to the service component to use. By default is **https://wenet.u-hopper.com/prod/service**.
 - **WENET_INTERACTION_PROTOCOL_ENGINE_API** to define the path to the interaction protocol engine component to use. By default is **https://wenet.u-hopper.com/prod/interaction_protocol_engine**.
 - **CACHE_TIMEOUT** to define the time in seconds that a value can be on the cache. By default is **300**.
 - **CACHE_SIZE** to define the maximum number of entries that can be on the cache. By default is **10000**.

### Show running logs

When the container is ready you can access the logs of the component, following the next steps:

 - Discover the identifier of the container of the component (`docker container ls`).
 - Open a shell to the container of the component (`docker exec -it <CONTAINER_NAME> /bin/bash`).
 - The logs are on the directory **/usr/wenet/task-manager/var/log**.

### Run performance test

This component provides a performance test using [K6](https://k6.io/). To run this test use the script `./runPerformanceTest.sh`.
By default, it is run over the development server, if you want to test another server pass the environment property **TASK_MANAGER_API**,
and also you can pass any parameter to configure **k6**. For example to run the test over the production one with 10 virtual users
during 30 seconds execute:

```
./runPerformanceTest.sh -e TASK_MANAGER_API="https://wenet.u-hopper.com/prod/task_manager" --vus 10 --duration 30s
```
This create the generic docker image, but you can create a different wit the **docker build** comma

## Documentation

The latest APIs documentation is available [here](http://swagger.u-hopper.com/?url=https://github.com/InternetOfUs/components-documentation/raw/master/sources/wenet-task_manager-openapi.yaml).


## Instances

The task manager has the next available instances:

 - WeNet production task manager API is available at [https://wenet.u-hopper.com/prod/task_manager](https://wenet.u-hopper.com/prod/task_manager/help/info).
 - WeNet development task manager API is available at [https://wenet.u-hopper.com/dev/task_manager](https://wenet.u-hopper.com/dev/task_manager/help/info).
 - The IIIA stable task manager API is available at [http://ardid.iiia.csic.es/wenet/task-manager/prod](http://ardid.iiia.csic.es/wenet/task-manager/latest/help/info).
 - The IIIA development task manager API is available at [http://ardid.iiia.csic.es/wenet/task-manager/dev](http://ardid.iiia.csic.es/wenet/task-manager/ldev/help/info).


## License

This software is under the [Apache V2 license](LICENSE)


## Interaction with other WeNet components

### [Profile manager](https://hub.docker.com/r/internetofus/profile-manager)

 - Used to validate that an user is defined (GET {{profile_manager_api}}/profiles/{{userId}}).


### [Service](https://hub.docker.com/r/internetofus/service-api)

 - Used to validate that an application is defined (GET {{service_api}}/app/{{appId}}).


### [Interaction protocol engine](https://hub.docker.com/r/internetofus/interaction-protocol-engine)

 - Convert any received transaction into a message to post. (POST {{interaction_protocol_engine_api}}/messages)


## Contact

### Researcher

 - [Nardine Osman](http://www.iiia.csic.es/~nardine/) ( [IIIA-CSIC](https://www.iiia.csic.es/~nardine/) ) nardine (at) iiia.csic.es
 - [Carles Sierra](http://www.iiia.csic.es/~sierra/) ( [IIIA-CSIC](https://www.iiia.csic.es/~sierra/) ) sierra (at) iiia.csic.es

### Developers

 - Joan Jené ( [UDT-IA, IIIA-CSIC](https://www.iiia.csic.es/people/person/?person_id=19) ) jjene (at) iiia.csic.es
 - Bruno Rosell i Gui ( [UDT-IA, IIIA-CSIC](https://www.iiia.csic.es/people/person/?person_id=27) ) rosell (at) iiia.csic.es
