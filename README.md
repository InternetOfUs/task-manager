# WeNet - Task manager

## Introduction

The task manager component is the one responsible for storing and maintaining the task and task types, and start the actions that can modify the task state.

A task is considered an instance of a task type. This task type contains the description of the attributes necessary to define the task,
the list of possible transactions (actions) that can be done in the task, and a set of norms that define how the task can change its state.
For example a simplified task type to organize a dinner with friends, can be:
 - **Attributes**
  - **when**: the dinner will be
  - **where**: the dinner will be.
 - **Transactions**
  - **accept**: when a user accepts to attend the dinner.
  - **decline**: when a user declines to attend the dinner.
  - **close**: when no more users can apply to be on the dinner.
  - **cancel**: when the user that organizes the dinner cancels.
 - **Norms**
  - When task created, therefore, notify my friends to participate and mark as open and add friends to unanswered
  - When user accept therefore inform task requester user accepts and add a user to attenders and remove from unanswered
  - When a user declines, therefore, inform add a user to declined and remove from unanswered
  - When requester close therefore inform to accepted friends that the dinner is set and unanswered friend that the dinner is cancelled and mark it as closed
  - When requester cancel therefore inform to an accepted and unanswered friend that the dinner is cancelled and mark it as closed

So the attributes a task of this type when some users has accepted an others has declined, can be:
 - **when**: Saturday night
 - **where**: Giorgios restaurant on the main street
 - **state**: Open
 - **unanswered**: User2, User89, user78
 - **declined**: User67
 - **attenders**: User1, User34

The transactions can be considered as asynchronous actions that can be done to change the task state.
When a user, an application or other WeNet component wants to change the state of a task, it has to
post a transaction to the task manager. It checks that the transaction is correct according to
the task type, and after that, the transaction is sent to the interaction protocol engine to verify
the task, community and user norms. In other words, the changes of the state are done by the norms
that are evaluated on the interaction protocol engine, and not by the task manager after receiving
a transaction.


## Setup and configuration

First of all, you must install the next software.

 - [docker](https://docs.docker.com/install/)
 - [docker compose](https://docs.docker.com/compose/install/)

### Requirements

The profile manager component requires:

 - [MongoDB](https://docs.mongodb.com/manual/installation/)
 - [WeNet - Profile manager](https://bitbucket.org/wenet/profile-manager/)
 - [WeNet - Interaction protocol engine](https://bitbucket.org/wenet/wenet-interaction-protocol-engine/)
 - [WeNet - Service API](https://bitbucket.org/wenet/wenet-service-api/)

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
 - **DEFAULT_WENET_PROFILE_MANAGER_API** to define the path to the profile manager component to use. By default is **"https://wenet.u-hopper.com/prod/profile_manager**.
 - **DEFAULT_WENET_SERVICE_API** to define the path to the service component to use. By default is **"https://wenet.u-hopper.com/prod/service**.
 - **DEFAULT_WENET_INTERACTION_PROTOCOL_ENGINE_API** to define the path to the interaction protocol engine component to use. By default is **"https://wenet.u-hopper.com/prod/interaction_protocol_engine**.

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
 - **WENET_PROFILE_MANAGER_API** to define the path to the profile manager component to use. By default is **"https://wenet.u-hopper.com/prod/profile_manager**.
 - **WENET_SERVICE_API** to define the path to the service component to use. By default is **"https://wenet.u-hopper.com/prod/service**.
 - **WENET_INTERACTION_PROTOCOL_ENGINE_API** to define the path to the interaction protocol engine component to use. By default is **"https://wenet.u-hopper.com/prod/interaction_protocol_engine**.
 - **COMP_AUTH_KEY** to define the authentication key that the componet has to use to interact with the other WeNet components.

When the container is started, it generates log files at the directory **/usr/wenet/profile-manager/var/log**.
The files with the **WeNet component logs** are generated in a rolling file with the name **wenet_X.log** ( where X is a number between 0 and 99).

If you want to start also a database and link both you can use the defined docker compose configuration.

```
docker-compose -f src/main/docker/docker-compose.yml up -d
```

This docker compose has the next variables:

 - **TASK_MANAGER_API_PORT** to define the port to listen for the API calls. By default is **8083**.
 - **MONGO_ROOT_USER** to define the root user for the MongoDB. By default is **root**.
 - **MONGO_ROOT_PASSWORD** to define the password of the root user for the MongoDB. By default is **password**.
 - **WENET_PROFILE_MANAGER_API** to define the path to the task manager component to use. By default is **"https://wenet.u-hopper.com/prod/profile_manager**.
 - **WENET_SERVICE_API** to define the path to the service component to use. By default is **"https://wenet.u-hopper.com/prod/service**.
 - **WENET_INTERACTION_PROTOCOL_ENGINE_API** to define the path to the interaction protocol engine component to use. By default is **"https://wenet.u-hopper.com/prod/interaction_protocol_engine**.

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

The latest APIs documentation is available [here](http://swagger.u-hopper.com/?url=https://bitbucket.org/wenet/wenet-components-documentation/raw/master/sources/wenet-task_manager-openapi.yaml).


## Instances

The task manager has the next available instances:

 - WeNet production task manager API is available at [https://wenet.u-hopper.com/prod/task_manager](https://wenet.u-hopper.com/prod/task_manager/help/info).
 - WeNet development task manager API is available at [https://wenet.u-hopper.com/dev/task_manager](https://wenet.u-hopper.com/dev/task_manager/help/info).
 - The IIIA stable task manager API is available at [http://ardid.iiia.csic.es/wenet/task-manager/prod](http://ardid.iiia.csic.es/wenet/task-manager/latest/help/info).
 - The IIIA development task manager API is available at [http://ardid.iiia.csic.es/wenet/task-manager/dev](http://ardid.iiia.csic.es/wenet/task-manager/ldev/help/info).


## License

This software is under the [MIT license](LICENSE)


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
