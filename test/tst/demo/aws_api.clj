(ns tst.demo.aws-api
  (:use demo.aws-api
        tupelo.core
        tupelo.test)
  (:require
    [clojure.java.io :as io]
    [clojure.test]
    [cognitect.aws.client.api :as aws]
    [cognitect.aws.credentials :as credentials]
    [tupelo.java-time :as jt]
    ))


(def minio-credentials (credentials/basic-credentials-provider {:access-key-id     "minioadmin"
                                                                :secret-access-key "minioadmin"}))
(def s3-client (aws/client {:api                  :s3
                            :region               "us-east-1" ; any legal value accepted
                            :credentials-provider minio-credentials
                            :endpoint-override    {:protocol :http
                                                   :hostname "localhost"
                                                   :port     19000}}))

(verify
  (let [bucket-name "instants"
        tmp-file    "/tmp/instant.txt"
        key-name    "instant"
        >>          (delete-bucket-force s3-client bucket-name)
        buckets     (grab :Buckets (aws/invoke s3-client {:op :ListBuckets}))
        dummy-str   (jt/->str-iso-nice (jt/now->Instant))]
    (is= [] buckets) ; empty

    ; make a bucket
    (create-bucket s3-client bucket-name)
    (let [r2 (grab :Buckets (aws/invoke s3-client {:op :ListBuckets}))]
      (is (submatch? [{:Name bucket-name}] r2))

      ; (spyx-pretty (aws/doc s3-client :PutObject))
      ; (spyx-pretty (aws/doc s3-client :GetObject))

      (spit tmp-file dummy-str)
      (aws/invoke s3-client
        {:op      :PutObject
         :request {:Bucket bucket-name
                   :Key    key-name
                   :Body   (io/input-stream tmp-file)}})

      (let [content-str (get-bucket-key s3-client bucket-name key-name)]
        (spyx-pretty content-str)
        (is= dummy-str content-str))

      (when false ; normally, just leave latest value for browser inspection
        (delete-bucket-force s3-client bucket-name))
      )))

(verify
  (let [bucket-name  "dummy"
        tmp-file     "/tmp/dummy2.txt"
        key-name     "instant"

        >>           (delete-bucket-force s3-client bucket-name)
        buckets      (grab :Buckets (aws/invoke s3-client {:op :ListBuckets}))
        buckets-keep (keep-if #(= bucket-name (grab :Name %)) buckets)
        dummy-str    "my dummy text file"]
    (is= [] (spyx-pretty buckets-keep)) ; empty

    ; make a bucket
    (create-bucket s3-client bucket-name)
    (let [r2 (grab :Buckets (aws/invoke s3-client {:op :ListBuckets}))]
      (is (submatch? [{:Name bucket-name}] r2))

      ; (spyx-pretty (aws/doc s3-client :PutObject))
      ; (spyx-pretty (aws/doc s3-client :GetObject))

      (spit tmp-file dummy-str)
      (aws/invoke s3-client
        {:op      :PutObject
         :request {:Bucket bucket-name
                   :Key    key-name
                   :Body   (io/input-stream tmp-file)}})

      (let [out-str (get-bucket-key s3-client bucket-name key-name)]
        (is= dummy-str out-str))

      (delete-bucket-force s3-client bucket-name))))
