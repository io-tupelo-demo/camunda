(ns tst.demo.aws-s3-api
  (:use demo.aws-api
        tupelo.core
        tupelo.test)
  (:require
    [clojure.test]
    [cognitect.aws.client.api :as aws]
    [cognitect.aws.credentials :as credentials]
    [environ.core :as environ]
    [demo.os-utils :as os]
    [schema.core :as s]
    [tupelo.java-time :as jt]
    [tupelo.string :as str]
    ))

;---------------------------------------------------------------------------------------------------
; #todo #awt clean up to use profiles.clj and environ lib
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
       :secret-access-key (environ/env :secret-key)})
    ))

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
(def key-name "now-tmp")
(def tmp-file "/tmp/dummy.txt")

(verify
  (when false ; enable for debug prints
    (spyx s3-keys)
    (spyx s3-creds-provider)
    (spyx s3-client-opts)
    (spyx s3-client)
    (spyx bucket-name))

  ; For local testing, cleanup any remaining bucket from last run
  (when (os/is-mac?)
    (let [delete-result (delete-bucket-force s3-client bucket-name)]
      ; (spyx-pretty delete-result)
      ))

  ; List/print existing buckets if desired
  (let [aws-result (aws/invoke s3-client {:op :ListBuckets})
        buckets    (:Buckets aws-result)]
    ; (spyx-pretty aws-result)
    ; (spyx-pretty buckets)

    ; Local testing
    (when (os/is-mac?)
      (is= [] buckets) ; ensure clean env

      ; Make a test bucket
      (let [create-result (create-bucket s3-client bucket-name)
            >>            (spyx create-result)
            r2            (grab :Buckets (aws/invoke s3-client {:op :ListBuckets}))]
        (is (submatch? [{:Name bucket-name}] r2)))))

  ; Add object to S3
  (let [dummy-str (jt/->str-iso-nice (jt/now->Instant))]
    (spit tmp-file dummy-str)
    (put-object s3-client bucket-name key-name tmp-file)

    ; Get object from S3 and verify contents
    (let [content-str (get-object s3-client bucket-name key-name)]
      (spyx-pretty content-str)
      (is= dummy-str content-str)))

  ; Delete object from S3
  (let [delete-result (aws/invoke s3-client
                        {:op      :DeleteObject
                         :request {:Bucket bucket-name
                                   :Key    key-name}})]
    (is= {} delete-result)
    ; Verify cannot delete the object twice (=> exception)
    (throws? (get-object s3-client bucket-name key-name))

    ; For local testing, we sometimes leave bucket & object for manual inspection
    ; using an S3 browser
    (when (os/is-mac?)
      (delete-bucket-force s3-client bucket-name))))
