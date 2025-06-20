(ns tst.demo.aws-api
  (:use demo.aws-api
        tupelo.core
        tupelo.test)
  (:require
    [clojure.test]
    [cognitect.aws.client.api :as aws]
    [cognitect.aws.credentials :as credentials]
    [tupelo.string :as str]
    ))

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
        ]
    (is= [] buckets) ; empty

    ; make a bucket
    (let [r1 (aws/invoke s3-client
                         {:op      :CreateBucket
                          :request {:Bucket "buck"}})
          r2 (grab :Buckets (aws/invoke s3-client {:op :ListBuckets}))
          ]
      (is (submatch? [{:Name "buck"}] r2))

      (delete-bucket-force s3-client "buck")
      )))

(verify
  (let [s3 (aws/client {:api :s3})
        ]

    #_(binding [*print-length* 1]
        (spyx-pretty (aws/ops s3)))

    (spyx-pretty (aws/doc s3 :CreateBucket))
    (spyx (aws/validate-requests s3 true))

    (do
      (aws/invoke s3 {:op :ListBuckets})
      ;; => {:Buckets [{:Name <name> :CreationDate <date> ,,,}]}

      ;; http-request and http-response are in the metadata
      (meta *1)
      ;; => {:http-request {:request-method :get,
      ;;                    :scheme :https,
      ;;                    :server-port 443,
      ;;                    :uri "/",
      ;;                    :headers {,,,},
      ;;                    :server-name "s3.amazonaws.com",
      ;;                    :body nil},
      ;;     :http-response {:status 200,
      ;;                     :headers {,,,},
      ;;                     :body <input-stream>}

      ;; create a bucket in the same region as the client
      (aws/invoke s3 {:op :CreateBucket :request {:Bucket "my-unique-bucket-name"}})

      ;; create a bucket in a region other than us-east-1
      (aws/invoke s3 {:op :CreateBucket :request {:Bucket "my-unique-bucket-name-in-us-west-1"
                                                  :CreateBucketConfiguration
                                                  {:LocationConstraint "us-west-1"}}})

      ;; NOTE: be sure to create a client with region "us-west-1" when accessing that bucket.

      (aws/invoke s3 {:op :ListBuckets})

      )
    ))
