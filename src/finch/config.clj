(ns finch.config
  (:use tupelo.core
        tupelo.test)
  (:require
    [cognitect.aws.client.api :as aws]
    [cognitect.aws.credentials :as credentials]
    [demo.os-utils :as os]
    [environ.core :as environ]
    [schema.core :as s]
    [tupelo.schema :as tsk]
    ))

(def s3-keys
  (if (not (os/is-linux?))
    (do   ; laptop testing env - minio default creds
      (prn :local-testing--minio)
      {:access-key-id     "minioadmin"
       :secret-access-key "minioadmin"})

    ; QA or prod:  get from Linux env var
    ; *** WARNING *** note inconsistent naming of both keys
    (do
      (prn :linux-testing--s3-via-environ)
      {:access-key-id     (environ/env :access-key)
       :secret-access-key (environ/env :secret-key)})))

(def s3-creds-provider (credentials/basic-credentials-provider s3-keys))
(def s3-client-opts
  (cond-it-> {:api                  :s3
              :region               "us-east-1" ; any legal value accepted
              :credentials-provider s3-creds-provider}

    ; required for local testing with minio
    (not (os/is-linux?)) (assoc it :endpoint-override {:protocol :http
                                                       :hostname "localhost"
                                                       :port     19000})))
(def s3-client (aws/client s3-client-opts))

(def bucket-name
  (if (os/is-linux?)
    "lambdawerk-qa-testcases-and-data" ; must use pre-existing bucket on heron-qa
    ; else
    "dummy")) ; for mac testing

