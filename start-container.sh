#!/bin/bash

# Start all containers with docker-compose
docker-compose -f docker-compose.yml up -d gpg_agent redis

docker exec -it hiking-alerts_gpg_agent_1 bash -c "pass show keyvault/password > /dev/null 2>&1"

echo "GPG key has been imported into the container."

docker-compose -f docker-compose.yml up -d spring-boot-app nginx angular