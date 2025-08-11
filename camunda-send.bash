#!/bin/bash
set -v

curl  -X POST  -H "Content-Type: application/json"  -d @data.json  \
  http://localhost:8080/engine-rest/process-definition/key/demo-process/start
