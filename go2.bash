#!/bin/bash

set -v

curl -H "Content-Type: application/json" -X POST  \
  -d '{"variables": {"bucket": {"value":"buck"}, "key": {"value":"sample.txt"} } }'  \
  http://localhost:8080/engine-rest/process-definition/key/aaa-newfile-process/start

