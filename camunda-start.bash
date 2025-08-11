#!/bin/bash
set -v
podman run -d  --name camunda  -p 8080:8080  docker.repo1.uhc.com/camunda/camunda-bpm-platform:latest
set +v
echo "http://localhost:8080/camunda"
echo "login:  demo/demo"
