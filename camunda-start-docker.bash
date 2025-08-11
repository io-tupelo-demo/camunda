#!/bin/bash
set -v
docker run -d --name camunda -p 8080:8080 camunda/camunda-bpm-platform:latest
set +v
echo "open browser with url: http://localhost:8080/camunda-welcome/index.html"
echo "login:  demo/demo"

