#!/bin/bash
set -v
s3cmd put   data-834/HT007992-001_20220112002237_HT000004-002-100005084.834     \
            s3://lambdawerk-qa-testcases-and-data

