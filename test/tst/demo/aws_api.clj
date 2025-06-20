(ns tst.demo.aws-api
  (:use demo.aws-api
        tupelo.core
        tupelo.test)
  (:require
    [clojure.java.io :as io]
    [clojure.test]
    [cognitect.aws.client.api :as aws]
    [cognitect.aws.credentials :as credentials]
    [tupelo.string :as str]
    )
  (:import [java.io File]))

(verify-focus
  (let [minio-credentials (credentials/basic-credentials-provider {:access-key-id     "minioadmin"
                                                                   :secret-access-key "minioadmin"})
        s3-client         (aws/client {:api                  :s3
                                       :region               "us-east-1" ; *** DUMMY VALUE ***
                                       :credentials-provider minio-credentials
                                       :endpoint-override    {:protocol :http
                                                              :hostname "localhost"
                                                              :port     19000}})
        >>                (delete-bucket-force s3-client "buck")
        buckets           (grab :Buckets (aws/invoke s3-client {:op :ListBuckets}))
        dummy-str "my dummy text file"
        ]
    (is= [] buckets) ; empty

    ; make a bucket
    (create-bucket s3-client "buck")
    (let [r2 (grab :Buckets (aws/invoke s3-client {:op :ListBuckets}))]
      (is (submatch? [{:Name "buck"}] r2))

      ; (spyx-pretty (aws/doc s3-client :PutObject))
      ; (spyx-pretty (aws/doc s3-client :GetObject))

      (spit "/tmp/dummy1.txt" dummy-str)
      (aws/invoke s3-client
                  {:op :PutObject
                   :request {:Bucket "buck"
                             :Key "dummy1.txt"
                             :Body   (io/input-stream "/tmp/dummy1.txt")}})

      (let [out-str (get-file-content s3-client "buck" "dummy1.txt")]
        (is= dummy-str out-str))

      (delete-bucket-force s3-client "buck"))))

