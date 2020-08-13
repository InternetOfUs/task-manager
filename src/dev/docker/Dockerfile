# syntax=docker/dockerfile:experimental

FROM maven:3.6.3-adoptopenjdk-11

WORKDIR /app

COPY pom.xml .
RUN --mount=type=cache,id=mvn-cache,target=/root/.m2 mvn dependency:go-offline


CMD ["bash"]