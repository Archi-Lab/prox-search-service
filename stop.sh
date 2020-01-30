#!/usr/bin/env bash

export IMAGE="$(./mvnw org.apache.maven.plugins:maven-help-plugin:3.2.0:evaluate -Dexpression=project.artifactId -q -DforceStdout)"

docker stack rm "${IMAGE}"
