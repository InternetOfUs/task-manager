# WeNet - Task manager

## Introduction

The task manager component is the one responsible for storing and maintaining the task and task types, and start the actions that can modify the task state.

A task is considered an instance of a task type. This task type contains the description of the attributes necessary to define the task,
the list of possible transactions (actions) that can be done in the task,  and a set of norms that define how the task can change its state.
For example a simplified task type to organize a dinner with friends, can be:
 - **Attributes**
    - **when**: the dinner will be
    - **where**: the dinner will be.
 - **Transactions**
    - **accept**: when an user accepts to attend the dinner.
    - **decline**:when an user declines to attend the dinner.
    - **close**: when no more users can apply to be on the dinner.
    - **cancel**:when the user that organizes the dinner cancels.
 - **Norms**
    - When task created therefore notify my friends to participate and mark as open and add friends to unanswered
    - When user accept therefore inform task requester user accepts and add user to attenders and remove from unanswered
    - When user declines therefore inform add user to declined and remove from unanswered
    - When requester close therefore inform to accepted friends that the dinner is set and unanswered friend that the dinner is cancelled and mark it as closed
    - When requester cancel therefore inform to accepted and unanswered friend that the dinner is cancelled and mark it as closed

So the attributes a task of this type when some users has accepted an others has declined, can be:
 - **when**: Saturday night
 - **where**: Giorgios restaurant on the main street
 - **state**: Open
 - **unanswered**: User2, User89, user78
 - **declined**: User67
 - **attenders**: User1, User34

The transactions can be considered as asynchronous actions that can be done to change the task state.
When an user, an application or other WeNet component wants to change the state of a task, it has to
post a transaction to the task manager. It checks that  the transaction is correct according to
the task type, and after that the transaction is sent to the interaction protocol engine to verify
the task, community and user norms. In other words, the changes of the state are done by the norms
that are evaluated on the interaction protocol engine, and not by the task manager after receiving
a transaction.


## Setup and configuration

### Installation

The task manager component required [Java version 11](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html) or higher.

All required java packages will be automatic installed by the compilation tool (`./mvnw clean install`).

### Requirements

The task manager component requires:

 - [MongoDB](https://docs.mongodb.com/manual/installation/)
 - [WeNet - Profile manager](https://bitbucket.org/wenet/profile-manager/)
 - [WeNet - Interaction protocol engine](https://bitbucket.org/wenet/wenet-interaction-protocol-engine/)
 - [WeNet - Service API](https://bitbucket.org/wenet/wenet-service-api/)


### Docker support

To use this feature you must to install the next software.

 - [docker](https://docs.docker.com/install/)
 - [docker compose](https://docs.docker.com/compose/install/)


#### Create docker image

If you want to create an image execute the next command.

```
./buildDockerImage.sh
```

This create the generic docker image, but you can create a different wit the **docker build** command and using the next arguments:

 - **DEFAULT_API_HOST** to define the default host where API will be bind. By default is **0.0.0.0**.
 - **DEFAULT_API_PORT** to define the default port where API will be bind. By default is **8080**.
 - **DEFAULT_DB_HOST** to define the default mongo database server host name. By default is **localhost**.
 - **DEFAULT_DB_PORT** to define the default mongo database server port. By default is **27017**.
 - **DEFAULT_DB_NAME** to define the default mongo database name. By default is **wenetTaskManagerDB**.
 - **DEFAULT_DB_USER_NAME** to define the default mongo database user name. By default is **wenetTaskManager**.
 - **DEFAULT_DB_USER_PASSWORD** to define the default mongo database user password. By default is **password**.
 - **DEFAULT_WENET_PROFILE_MANAGER_API** to define the path to the profile manager component to use. By default is **"https://wenet.u-hopper.com/prod/profile_manager**.
 - **DEFAULT_WENET_INTERACTION_PROTOCOL_ENGINE_API** to define the path to the interaction protocol engine component to use. By default is **"https://wenet.u-hopper.com/prod/interaction_protocol_engine**.
 - **DEFAULT_WENET_SERVICE_API** to define the path to the service component to use. By default is **"https://wenet.u-hopper.com/prod/service**.

This arguments are used to create a configurations files at **/usr/wenet/task-manager/etc**.
So you can mount a volume to this if you want to modify any configuration property at runtime.

#### Run docker image

To run a the created docker image, run the next command:

```
docker run -t -i -p 8080:8080 --name wenet_task_manager_api wenet/task-manager
```

You can modify use the next environment properties to modify some parameters of the server:

 - **API_HOST** to define the host where the API has to bind. By default is **0.0.0.0**.
 - **API_PORT** to define the port where the API has to bind. By default is **8080**.
 - **DB_HOST** to define the mongo database server host name. By default is **localhost**.
 - **DB_PORT** to define the mongo database server port. By default is **27017**.
 - **DB_NAME** to define the mongo database name. By default is **wenetTaskManagerDB**.
 - **DB_USER_NAME** to define the mongo database user name. By default is **wenetTaskManager**.
 - **DB_USER_PASSWORD** to define the mongo database user password. By default is **password**.
 - **WENET_PROFILE_MANAGER_API** to define the path to the profile manager component to use. By default is **"https://wenet.u-hopper.com/prod/profile_manager**.
 - **WENET_INTERACTION_PROTOCOL_ENGINE_API** to define the path to the interaction protocol engine component to use. By default is **"https://wenet.u-hopper.com/prod/interaction_protocol_engine**.
 - **WENET_SERVICE_API** to define the path to the service component to use. By default is **"https://wenet.u-hopper.com/prod/service**.

Also you can define your own configuration that modify this properties and mount to  **/usr/wenet/task-manager/etc**.

If you want to start also a database and link both you can use the docker compose (`docker-compose -f src/main/docker/docker-compose.yml up -d`). To modify the component to links or the port to deploy use the next variables:

 - **TASK_MANAGER_API_PORT** to define the port to listen for the API calls. By default is **8082**.
 - **MONGO_ROOT_USER** to define the root user for the MongoDB. By default is **root**.
 - **MONGO_ROOT_PASSWORD** to define the password of the root user for the MongoDB. By default is **password**.
 - **WENET_PROFILE_MANAGER_API** to define the path to the profile manager component to use. By default is **"https://wenet.u-hopper.com/prod/profile_manager**.
 - **WENET_INTERACTION_PROTOCOL_ENGINE_API** to define the path to the interaction protocol engine component to use. By default is **"https://wenet.u-hopper.com/prod/interaction_protocol_engine**.
 - **WENET_SERVICE_API** to define the path to the service component to use. By default is **"https://wenet.u-hopper.com/prod/service**.

When the container is ready you can access the logs of the component, following the next steps:

 - Discover the identifier of the container of the component (`docker container ls`).
 - Open a shell to the container of the component (`docker exec -it c82f8f4a136c /bin/bash`).
 - The logs are on the directory **/usr/wenet/task-manager/var/log**.


## Usage

The project use the [Apache maven](https://maven.apache.org/) tool to solve the dependencies,
generate the Open API documentation, compile the component and run the test.

 - Use `./mvnw dependency:list` to show the component dependencies.
 - Use `./mvnw compile` to compile and generate the Open API documentation (**target/classes/wenet-task_manager-openapi.yml**).
 - Use `./mvnw tests` to run the test.
 - Use `./mvnw site` to generate a HTML page (**target/site/index.html**) with all the reports (test, javadoc, PMD,CPD and coverage).


### Run and configure

We encourage you to use the docker image of this component instead the next commands, because it is easier to use.

If you want to run this component you must to follow the next steps:

 - Compile the project (`./mvnw clean install`)
 - On the directory where you want to install the component (for example **~/task-manager**) create the directories **etc** and **lib**.
 - Copy the compiled jar (`cp target/wenet-task-manager-VERSION.jar ~/task-manager/.`).
 - Copy the jar dependencies (`cp target/lib/* ~/task-manager/lib/.`).
 - Copy the default logging configuration (`cp src/main/resources/tinylog.properties ~/task-manager/etc/log_configuration.properties.`).
 - Copy the default component configuration (`cp src/main/resources/wenet-task-manager.configuration.json ~/task-manager/etc/configuration.conf.`).
 - Edit the component configuration to fix the URL of the other components and the database connection.
 - Go to the install directory and execute the command `java -jar -Dtinylog.configuration=etc/log_configuration.properties wenet-task-manager-VERSION.jar -c etc`.


## Documentation

The latest APIs documentation is available [here](http://swagger.u-hopper.com/?url=https://bitbucket.org/wenet/wenet-components-documentation/raw/master/sources/wenet-task_manager-openapi.yaml).


## Instances

The task manager has the next available instances:

 - WeNet production task manager API is available at [https://wenet.u-hopper.com/prod/task_manager/](https://wenet.u-hopper.com/prod/task_manager/).
 - WeNet development task manager API is available at [https://wenet.u-hopper.com/dev/task_manager/](https://wenet.u-hopper.com/dev/task_manager/).
 - The IIIA stable task manager API is available at [http://ardid.iiia.csic.es/wenet/task-manager/latest/](http://ardid.iiia.csic.es/wenet/task-manager/latest/).
 - The IIIA development task manager API is available at [http://ardid.iiia.csic.es/wenet/task-manager/dev/](http://ardid.iiia.csic.es/wenet/task-manager/ldev/).
 - The task manager API 0.4.X is available at [http://ardid.iiia.csic.es/wenet/task-manager/0.4/](http://ardid.iiia.csic.es/wenet/task-manager/0.4/).
 - The task manager API 0.3.X is available at [http://ardid.iiia.csic.es/wenet/task-manager/0.3/](http://ardid.iiia.csic.es/wenet/task-manager/0.3/).
 - The task manager API 0.2.0 is available at [http://ardid.iiia.csic.es/wenet/task-manager/0.2.0/](http://ardid.iiia.csic.es/wenet/task-manager/0.2.0/).


## License

This software is under the [MIT license](LICENSE)


## Interaction with other WeNet components

### Profile manager

 - Used to validate that an user is defined (GET {{profile_manager_api}}/profiles/{{userId}}).
 

### Service

 - Used to validate that an application is defined (GET {{service_api}}/app/{{appId}}).
 

### Interaction protocol engine

 - Convert any received transaction into a message to post. (POST {{interaction_protocol_engine_api}}/messages)


## Contact

### Researcher

 - [Nardine Osman](http://www.iiia.csic.es/~nardine/) ( [IIIA-CSIC](https://www.iiia.csic.es/~nardine/) ) nardine (at) iiia.csic.es
 - [Carles Sierra](http://www.iiia.csic.es/~sierra/) ( [IIIA-CSIC](https://www.iiia.csic.es/~sierra/) ) sierra (at) iiia.csic.es

### Developers

 - Joan Jené ( [UDT-IA, IIIA-CSIC](https://www.iiia.csic.es/people/person/?person_id=19) ) jjene (at) iiia.csic.es
 - Bruno Rosell i Gui ( [UDT-IA, IIIA-CSIC](https://www.iiia.csic.es/people/person/?person_id=27) ) rosell (at) iiia.csic.es
