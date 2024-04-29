#!/bin/bash

# Remember to change the directory to the project root directory
./mvnw clean package -DskipTests && \
docker-compose build && \
docker-compose up