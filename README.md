# WeNet - Task manager

## Introduction

The task manager component is the one responsible for storing and maintaining the task done by the WeNet users.


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
docker build -f src/main/docker/Dockerfile -t wenet/task-manager .
```

You can use the next arguments:

 - **DEFAULT_API_HOST** to define the default host where API will be bind. By default is **0.0.0.0**.
 - **DEFAULT_API_PORT** to define the default port where API will be bind. By default is **8080**.
 - **DEFAULT_DB_HOST** to define the default mongo database server host name. By default is **localhost**.
 - **DEFAULT_DB_PORT** to define the default mongo database server port. By default is **27017**.
 - **DEFAULT_DB_NAME** to define the default mongo database name. By default is **wenetTaskManagerDB**.
 - **DEFAULT_DB_USER_NAME** to define the default mongo database user name. By default is **wenetTaskManager**.
 - **DEFAULT_DB_USER_PASSWORD** to define the default mongo database user password. By default is **password**.
 - **DEFAULT_WENET_PROFILE_MANAGER_API** to define the path to the profile manager component to use. By default is **https://wenet.u-hopper.com/profile_manager**.
 - **DEFAULT_WENET_INTERACTION_PROTOCOL_ENGINE_API** to define the path to the interaction protocol engine component to use. By default is **https://wenet.u-hopper.com/interaction_protocol_engine**.
 - **DEFAULT_WENET_SERVICE_API** to define the path to the service component to use. By default is **https://wenet.u-hopper.com/service**.

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
 - **WENET_PROFILE_MANAGER_API** to define the path to the profile manager component to use. By default is **https://wenet.u-hopper.com/profile_manager**.
 - **WENET_INTERACTION_PROTOCOL_ENGINE_API** to define the path to the interaction protocol engine component to use. By default is **https://wenet.u-hopper.com/interaction_protocol_engine**.
 - **WENET_SERVICE_API** to define the path to the service component to use. By default is **https://wenet.u-hopper.com/service**.

Also you can define your own configuration that modify this properties and mount to  **/usr/wenet/task-manager/etc**.

If you want to start also a database and link both you can use the docker compose (`docker-compose -f src/main/docker/docker-compose.yml up -d`):

After that you can interact with the API at **http://localhost:80**. You can modify the listening port
with the next environment properties:

 - **API_PORT** to define the port where the API has to bind to the localhost. By default is **80**.

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

 - WeNet production task manager API is available at [https://wenet.u-hopper.com/task_manager](https://wenet.u-hopper.com/task_manager).
 - WeNet development task manager API is available at [https://wenet.u-hopper.com/dev/task_manager](https://wenet.u-hopper.com/dev/task_manager).
 - The IIIA stable task manager API is available at [https://wenet.u-hopper.com/dev/task_manager](https://wenet.u-hopper.com/dev/task_manager).
 - The IIIA development task manager API is available at [https://wenet.u-hopper.com/dev/task_manager](https://wenet.u-hopper.com/dev/task_manager).
 - The task manager API 0.3.0 is available at [http://ardid.iiia.csic.es/wenet/task-manager/0.3.0/](http://ardid.iiia.csic.es/wenet/task-manager/0.3.0/).
 - The task manager API 0.2.0 is available at [http://ardid.iiia.csic.es/wenet/task-manager/0.2.0/](http://ardid.iiia.csic.es/wenet/task-manager/0.2.0/).
 - The task manager API 0.1.0 (Dummy version) is available at [http://ardid.iiia.csic.es/dev-wenet-task-manager/](http://ardid.iiia.csic.es/dev-wenet-task-manager/](http://ardid.iiia.csic.es/dev-wenet-task-manager/](http://ardid.iiia.csic.es/dev-wenet-task-manager/).


## License

This software is under the [MIT license](LICENSE)


## Contact

### Researcher

 - [Nardine Osman](http://www.iiia.csic.es/~nardine/) ( [IIIA-CSIC](https://www.iiia.csic.es/~nardine/) ) nardine (at) iiia.csic.es
 - [Carles Sierra](http://www.iiia.csic.es/~sierra/) ( [IIIA-CSIC](https://www.iiia.csic.es/~sierra/) ) sierra (at) iiia.csic.es

### Developers

 - Joan Jené ( [UDT-IA, IIIA-CSIC](https://www.iiia.csic.es/people/person/?person_id=19) ) jjene (at) iiia.csic.es
 - Bruno Rosell i Gui ( [UDT-IA, IIIA-CSIC](https://www.iiia.csic.es/people/person/?person_id=27) ) rosell (at) iiia.csic.es
