#!/bin/bash
set -v
docker run -d --name camunda -p 8080:8080 camunda/camunda-bpm-platform:latest
set +v
echo "http://localhost:8080/camunda"
echo "login:  demo/demo"

