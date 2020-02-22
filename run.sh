#!/usr/bin/env bash

export REPOSITORY="$(./mvnw org.apache.maven.plugins:maven-help-plugin:3.2.0:evaluate -Dexpression=docker.image.prefix -q -DforceStdout)"
export IMAGE="$(./mvnw org.apache.maven.plugins:maven-help-plugin:3.2.0:evaluate -Dexpression=project.artifactId -q -DforceStdout)"
REVISION="$(./mvnw org.apache.maven.plugins:maven-help-plugin:3.2.0:evaluate -Dexpression=revision -q -DforceStdout)"
CHANGELIST="$(./mvnw org.apache.maven.plugins:maven-help-plugin:3.2.0:evaluate -Dexpression=changelist -q -DforceStdout)"
export TAG="${REVISION}${CHANGELIST}"

docker network inspect prox >/dev/null 2>&1 || docker network create --driver=overlay prox

docker stack deploy -c src/main/docker/docker-compose.yml "${IMAGE}"
