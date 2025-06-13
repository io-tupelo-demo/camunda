#!/bin/bash

set -v

curl -H "Content-Type: application/json" -X POST  \
  -d '{"variables": {"bucket": {"value":"abc"}, "key": {"value":"def"} } }'  \
  http://localhost:8080/engine-rest/process-definition/key/Finch.Import834/start

