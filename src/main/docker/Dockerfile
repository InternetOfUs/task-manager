# --- Build the code
FROM maven:3.6.3-jdk-11-slim AS build
LABEL stage=build
ENV MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"
RUN mkdir -p /build
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src/ /build/src/
RUN mvn -DfinalName=wenet-task-manager -DskipTests install

# --- Run the code
FROM openjdk:11.0.5-jre
ARG DEFAULT_API_HOST=0.0.0.0
ARG DEFAULT_API_PORT=8080
ARG DEFAULT_DB_HOST=localhost
ARG DEFAULT_DB_PORT=27017
ARG DEFAULT_DB_NAME=wenetTaskManagerDB
ARG DEFAULT_DB_USER_NAME=wenetTaskManager
ARG DEFAULT_DB_USER_PASSWORD=password
ARG DEFAULT_WENET_PROFILE_MANAGER_API_HOST="wenet.u-hopper.com"
ARG DEFAULT_WENET_PROFILE_MANAGER_API_PORT=443
ARG DEFAULT_WENET_PROFILE_MANAGER_API_PATH="/profile_manager"
ARG DEFAULT_WENET_INTERACTION_PROTOCOL_ENGINE_API_HOST="wenet.u-hopper.com"
ARG DEFAULT_WENET_INTERACTION_PROTOCOL_ENGINE_API_PORT=443
ARG DEFAULT_WENET_INTERACTION_PROTOCOL_ENGINE_API_PATH="/interaction_protocol_engine"
ARG DEFAULT_WENET_SERVICE_API_HOST="wenet.u-hopper.com"
ARG DEFAULT_WENET_SERVICE_API_PORT=443
ARG DEFAULT_WENET_SERVICE_API_PATH="/service"
ENV API_HOST=${DEFAULT_API_HOST}
ENV API_PORT=${DEFAULT_API_PORT}
ENV DB_HOST=${DEFAULT_DB_HOST}
ENV DB_PORT=${DEFAULT_DB_PORT}
ENV DB_NAME=${DEFAULT_DB_NAME}
ENV DB_USER_NAME=${DEFAULT_DB_USER_NAME}
ENV DB_USER_PASSWORD=${DEFAULT_DB_USER_PASSWORD}
ENV WENET_PROFILE_MANAGER_API_HOST=${DEFAULT_WENET_PROFILE_MANAGER_API_HOST}
ENV WENET_PROFILE_MANAGER_API_PORT=${DEFAULT_WENET_PROFILE_MANAGER_API_PORT}
ENV WENET_PROFILE_MANAGER_API_PATH=${DEFAULT_WENET_PROFILE_MANAGER_API_PATH}
ENV WENET_INTERACTION_PROTOCOL_ENGINE_API_HOST=${DEFAULT_WENET_INTERACTION_PROTOCOL_ENGINE_API_HOST}
ENV WENET_INTERACTION_PROTOCOL_ENGINE_API_PORT=${DEFAULT_WENET_INTERACTION_PROTOCOL_ENGINE_API_PORT}
ENV WENET_INTERACTION_PROTOCOL_ENGINE_API_PATH=${DEFAULT_WENET_INTERACTION_PROTOCOL_ENGINE_API_PATH}
ENV WENET_SERVICE_API_HOST=${DEFAULT_WENET_SERVICE_API_HOST}
ENV WENET_SERVICE_API_PORT=${DEFAULT_WENET_SERVICE_API_PORT}
ENV WENET_SERVICE_API_PATH=${DEFAULT_WENET_SERVICE_API_PATH}
ENV RUN_ARGS=""

# Create running dierectories
RUN mkdir -p /usr/wenet/task-manager/lib
RUN mkdir -p /usr/wenet/task-manager/etc
RUN mkdir -p /usr/wenet/task-manager/var/log

# Create configuration for api
RUN echo "{\"api\":{\"host\":\"${DEFAULT_API_HOST}\",\"port\":${DEFAULT_API_PORT}}}" > /usr/wenet/task-manager/etc/api.json

# Create configuration for persistence
RUN echo "{\"persistence\":{\"db_name\":\"${DEFAULT_DB_NAME}\",\"host\":\"${DEFAULT_DB_HOST}\",\"port\":${DEFAULT_DB_PORT},\"username\":\"${DEFAULT_DB_USER_NAME}\",\"password\":\"${DEFAULT_DB_USER_PASSWORD}\"}}" > /usr/wenet/task-manager/etc/persistence.json

# Create configuration for profile manager components
RUN echo "{\"wenetComponents\":{\"profileManager\":{\"host\":\"${DEFAULT_WENET_PROFILE_MANAGER_API_HOST}\",\"port\":${DEFAULT_WENET_PROFILE_MANAGER_API_PORT},\"apiPath\":\"${DEFAULT_WENET_PROFILE_MANAGER_API_PATH}\"}}}" > /usr/wenet/task-manager/etc/profileManagerApi.json

# Create configuration for interaction protocol engine component
RUN echo "{\"wenetComponents\":{\"interactionProtocolEngine\":{\"host\":\"${DEFAULT_WENET_INTERACTION_PROTOCOL_ENGINE_API_HOST}\",\"port\":${DEFAULT_WENET_INTERACTION_PROTOCOL_ENGINE_API_PORT},\"apiPath\":\"${DEFAULT_WENET_INTERACTION_PROTOCOL_ENGINE_API_PATH}\"}}}" > /usr/wenet/task-manager/etc/interactionProtocolEngine.json

# Create configuration for service component
RUN echo "{\"wenetComponents\":{\"service\":{\"host\":\"${DEFAULT_WENET_SERVICE_API_HOST}\",\"port\":${DEFAULT_WENET_SERVICE_API_PORT},\"apiPath\":\"${DEFAULT_WENET_SERVICE_API_PATH}\"}}}" > /usr/wenet/task-manager/etc/serviceApi.json

WORKDIR /usr/wenet/task-manager
COPY --from=build /build/target/wenet-task-manager.jar /usr/wenet/task-manager/wenet-task-manager.jar
COPY --from=build /build/target/lib/ /usr/wenet/task-manager/lib/


## Add the wait script to the image
ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.7.3/wait /wait
RUN chmod +x /wait

EXPOSE ${API_PORT}
CMD /wait && java -jar wenet-task-manager.jar -c etc \
  -papi.host=${API_HOST} -papi.port=${API_PORT}\
  -ppersistence.host=${DB_HOST} -ppersistence.port=${DB_PORT} -ppersistence.db_name=${DB_NAME} -ppersistence.username=${DB_USER_NAME} -ppersistence.password=${DB_USER_PASSWORD}\
  -pwenetComponents.profileManager.host=${WENET_PROFILE_MANAGER_API_HOST} -pwenetComponents.profileManager.port=${WENET_PROFILE_MANAGER_API_PORT} -pwenetComponents.profileManager.apiPath=${WENET_PROFILE_MANAGER_API_PATH}\
  -pwenetComponents.interactionProtocolEngine.host=${WENET_INTERACTION_PROTOCOL_ENGINE_API_HOST} -pwenetComponents.interactionProtocolEngine.port=${WENET_INTERACTION_PROTOCOL_ENGINE_API_PORT} -pwenetComponents.interactionProtocolEngine.apiPath=${WENET_INTERACTION_PROTOCOL_ENGINE_API_PATH}\
  -pwenetComponents.service.host=${WENET_SERVICE_API_HOST} -pwenetComponents.service.port=${WENET_SERVICE_API_PORT} -pwenetComponents.service.apiPath=${WENET_SERVICE_API_PATH}\
  ${RUN_ARGS}
