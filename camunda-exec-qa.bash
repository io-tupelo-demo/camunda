#!/bin/bash
echo ""
echo "  Type Ctrl-C to terminate server..."
echo ""
set -v
clojure -X finch.camunda-init/-main

