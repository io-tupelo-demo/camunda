#!/bin/bash
set -v
podman run -d  --name camunda  -p 8080:8080  docker.repo1.uhc.com/camunda/camunda-bpm-platform:latest
echo "login:  demo/demo"

