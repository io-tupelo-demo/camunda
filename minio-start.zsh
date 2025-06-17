#!/bin/zsh

set -v 
export MINIO_ADDRESS="localhost:19000"
export MINIO_CONSOLE_ADDRESS="localhost:19001"

minio server ~/minio

