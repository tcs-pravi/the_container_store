#!/usr/bin/env bash

# Use entrypoint provided by docker image: https://github.com/the-container-store/docker/blob/master/java-11-jre/entrypoint.ctmpl
if [ -f "/entrypoint.ctmpl" ]; then
  consul-template --once -template "/entrypoint.ctmpl:/home/app/entrypoint.sh" -log-level=info
  source /home/app/entrypoint.sh
fi

java \
  -Ddeployment.environment=${DEPLOYMENT_ENVIRONMENT} \
  -Ddeployment.stack=${DEPLOYMENT_STACK} \
  -Dapp.name=${APP_NAME} \
  -Dapp.version=${APP_VERSION} \
  -Dhostname=${HOSTNAME} \
  -Dlogging.console.enabled=true \
  -Dserver.use-forward-headers=true \
  ${JAVA_OPTS} \
  -jar /home/app/${APP_NAME}.jar
