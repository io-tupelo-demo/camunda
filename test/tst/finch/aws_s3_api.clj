(ns tst.finch.aws-s3-api
  (:use finch.aws-api
        tupelo.core
        tupelo.test)
  (:require
    [clojure.test]
    [cognitect.aws.client.api :as aws]
    [finch.os-utils :as os]
    [finch.config :as config]
    [schema.core :as s]
    [tupelo.java-time :as jt]
    ))

(def key-name "now-tmp")
(def tmp-file "/tmp/dummy.txt")

(verify
  (when true ; enable for debug prints
    (spyx config/s3-keys)
    (spyx config/s3-creds-provider)
    (spyx config/s3-client-opts)
    (spyx config/s3-client)

    (spyx config/bucket-name))

  ; For local testing, cleanup any remaining bucket from last run
  (when (os/is-mac?)
    (let [delete-result (delete-bucket-force config/s3-client config/bucket-name)]
      ; (spyx-pretty delete-result)
      ))

  ; List/print existing buckets if desired
  (let [aws-result (aws/invoke config/s3-client {:op :ListBuckets})
        buckets    (:Buckets aws-result)]
    ; (spyx-pretty aws-result)
    ; (spyx-pretty buckets)

    ; Local testing
    (when (os/is-mac?)
      (is= [] buckets) ; ensure clean env

      ; Make a test bucket
      (let [create-result (create-bucket config/s3-client config/bucket-name)
            >>            (spyx create-result)
            r2            (grab :Buckets (aws/invoke config/s3-client {:op :ListBuckets}))]
        (is (submatch? [{:Name config/bucket-name}] r2)))))

  ; Add object to S3
  (let [dummy-str (jt/->str-iso-nice (jt/now->Instant))]
    (spit tmp-file dummy-str)
    (put-object config/s3-client config/bucket-name key-name tmp-file)

    ; Get object from S3 and verify contents
    (let [content-str (get-object->str config/s3-client config/bucket-name key-name)]
      (spyx-pretty content-str)
      (is= dummy-str content-str)))

  ; Delete object from S3
  (let [delete-result (aws/invoke config/s3-client
                        {:op      :DeleteObject
                         :request {:Bucket config/bucket-name
                                   :Key    key-name}})]
    (is= {} delete-result)
    ; Verify cannot delete the object twice (=> exception)
    (throws? (get-object->str config/s3-client config/bucket-name key-name))

    ; For local testing, we sometimes leave bucket & object for manual inspection
    ; using an S3 browser
    (when (os/is-mac?)
      (delete-bucket-force config/s3-client config/bucket-name))))
