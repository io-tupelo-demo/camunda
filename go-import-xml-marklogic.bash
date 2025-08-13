#!/bin/bash
set -v

/usr/bin/mlcp  import                       \
 -host ${IP_ADDR_MARKLOGIC:-localhost}      \
 -port 8000                                 \
 -username admin                            \
 -password admin                            \
 -input_file_path ./data-xml

