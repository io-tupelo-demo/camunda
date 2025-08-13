#!/bin/bash
set -v
curl  -X POST  -H "Content-Type: application/json"  -d @data-qa.json  \
  http://${IP_ADDR_CAMUNDA:-localhost}:8080/engine-rest/process-definition/key/load-834-to-marklogic/start

