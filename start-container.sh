#!/bin/bash

# Start all containers with docker-compose
docker-compose -f docker-compose.yml up -d gpg_agent redis

IMAGE_NAME="pentaho-carte"
IS_SCHEDULER_RUNNING=false
if docker ps --filter "ancestor=$IMAGE_NAME" --format '{{.ID}}' | grep -q .; then
    IS_SCHEDULER_RUNNING=true
fi

docker exec -it hiking-alerts_gpg_agent_1 bash -c "pass show keyvault/password > /dev/null 2>&1"

echo "GPG key has been imported into the container."

docker-compose -f docker-compose.yml up -d spring-boot-app pentaho-carte nginx angular

{
  sleep 10

  if [ "$IS_SCHEDULER_RUNNING" = false ]; then
     curl --location --request GET 'https://[HOST]/kettle/executeJob?rep=&job=/opt/pentaho/repo/2025/jb_scheduler.kjb' \
--header 'Authorization: Basic YWRtaW46YWRtaW4='
  fi
} &
