(ns demo.aws-api
  (:use tupelo.core)
  (:require
    [cognitect.aws.client.api :as aws]
    [cognitect.aws.credentials :as credentials]
    [tupelo.string :as str]
    ))

; Step 1: List and delete all objects
(defn delete-all-objects
  [s3-client bucket-name]
  (let [list-response (aws/invoke s3-client
                                  {:op      :ListObjectsV2
                                   :request {:Bucket bucket-name}})
        objects       (get-in list-response [:Contents])]
    (when (seq objects)
      (aws/invoke s3-client
                  {:op      :DeleteObjects
                   :request {:Bucket bucket-name
                             :Delete {:Objects
                                      (mapv #(hash-map :Key (:Key %)) objects)}
                             }
                  }))) )

; Step 2: List and abort all multipart uploads
(defn abort-all-multipart-uploads
  [s3-client bucket-name]
  (let [list-response (aws/invoke s3-client
                                  {:op      :ListMultipartUploads
                                   :request {:Bucket bucket-name}})
        uploads       (get-in list-response [:Uploads])]
    (doseq [upload uploads]
      (aws/invoke s3-client
                  {:op      :AbortMultipartUpload
                   :request {:Bucket   bucket-name
                             :Key      (:Key upload)
                             :UploadId (:UploadId upload)}}))))

(defn delete-bucket-force
  [s3-client bucket-name]
  (delete-all-objects s3-client bucket-name)
  (abort-all-multipart-uploads s3-client bucket-name)

  (aws/invoke s3-client
              {:op :DeleteBucket
               :request {:Bucket bucket-name}}))