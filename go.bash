#!/bin/bash

set -v

curl -H "Content-Type: application/json" -X POST  \
  -d '{"variables": {"amount": {"value":345,"type":"integer"}, "item": {"value":"item-xyz"} } }'  \
  http://localhost:8080/engine-rest/process-definition/key/payment-process/start

