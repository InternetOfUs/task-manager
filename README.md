# WeNet - Task manager

## Introduction

The task manager component is the one responsible for storing and maintaining
the task and task types. 

A task is used to coordinate a set of WeNet users to do something. It is defined
by the next fields:

 * __id__  identifier of the task.
 * __goal__  the objective to reach. In other words why the users cooperate.
 * __requesterId__  the identifier of the user that has created the task.
 * **_creationTs**  the difference, measured in seconds, between the time when the task
 is created and midnight, January 1, 1970 UTC.
 * **_lastUpdateTs**  the difference, measured in seconds, between the time when the task
 is updated and midnight, January 1, 1970 UTC.
 * __closeTs__  the difference, measured in seconds, between the time when the task
 is considered done (closed) and midnight, January 1, 1970 UTC.
 * __appId__  identifier of the application where the task is associated.
 * __communityId__  identifier of the community where the task is associated.
 * __taskTypeId__  identifier of the type. It defines the common behaviours
 allowed to the users when interacting on the task.
 * __attributes__  the JSON object with the values that instantiate the task.
 The possible values are defined by the type.
 * __norms__  that modify the default behaviour defined by the type. If you want
to read more about how to define norms read the [WeNet developer documentation](https://internetofus.github.io/developer/docs/tech/conversation/norms)
 * __transactions__  the historic list of transactions that have been done in
 the task. Also, each transaction has the information of the application messages
 that have been sent to the users that are involved in the task.
 
On the other hand, the task type defines what the users can do in a task. For
this purpose has the next fields:

- **id**  identifier of the task type.
- **name**  of the type.
- **description**  of the type.
- **keywords**  used to define the type.
- **attributes**  is a JSON object where the fields are the possible attributes
 of the task, and the value is the name is the OpenAPI description of the possible
 values for the attribute.
- **transactions**  is a JSON object where the fields are the possible labels
of the transactions that the users can do on the task, and the value is the
OpenAPI description of the attributes for the transaction.
- **callbacks**  is a JSON object where the fields are the possible labels
of the messages that the norms can send to the application for a user, and
the value is the OpenAPI description of the attributes for the message.
- **norms**  that describe the possible behaviour can do in a task of this type.
 If you want to read more about how to define norms read the [WeNet developer documentation](https://internetofus.github.io/developer/docs/tech/conversation/norms)

The next JSON is an example of a task type that echo the received transaction
to the same user.

```json

{
   "id":"wenet_echo_v1",
   "name":"Echo",
   "description":"This tasks echo the transaction messages",
   "keywords":[
      "example",
      "test"
   ],
   "transactions":{
      "echo":{
         "type":"object",
         "description":"Send the echo message",
         "properties":{
            "message":{
               "type":"string",
               "description":"The message to echo"
            }
         }
      }
   },
   "callbacks":{
      "echo":{
         "type":"object",
         "properties":{
            "taskId":{
               "type":"string",
               "description":"The identifier of the task"
            },
            "communityId":{
               "type":"string",
               "description":"The identifier of the community"
            },
            "message":{
               "type":"string",
               "description":"The echo message"
            }
         }
      }
   },
   "norms":[
      {
         "whenever":"is_received_created_task()",
         "thenceforth":"add_created_transaction()"
      },
      {
         "whenever":"is_received_do_transaction('echo',Content)",
         "thenceforth":"add_message_transaction() and send_user_message('echo',Content)"
      }
   ]
}

```


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
 - **DEFAULT_WENET_PROFILE_MANAGER_API** to define the path to the profile manager component to use. By default is **https://wenet.u-hopper.com/prod/profile_manager**.
 - **DEFAULT_WENET_SERVICE_API** to define the path to the service component to use. By default is **https://wenet.u-hopper.com/prod/service**.
 - **DEFAULT_WENET_INTERACTION_PROTOCOL_ENGINE_API** to define the path to the interaction protocol engine component to use. By default is **https://wenet.u-hopper.com/prod/interaction_protocol_engine**.

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
