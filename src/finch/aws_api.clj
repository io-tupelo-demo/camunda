(ns finch.aws-api
  (:use tupelo.core)
  (:require
    [clojure.java.io :as io]
    [cognitect.aws.client.api :as aws]
    [schema.core :as s]
    [tupelo.string :as str]
    ))

;---------------------------------------------------------------------------------------------------
; Code to delete all objects in a bucket

; Step 1: List and delete all objects
(s/defn delete-all-objects
  [s3-client :- s/Any
   bucket-name :- s/Str]
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
                   }))))

; Step 2: List and abort all multipart uploads
(s/defn abort-all-multipart-uploads
  [s3-client :- s/Any
   bucket-name :- s/Str]
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

(s/defn delete-bucket-force
  "Two step process to delete a S3 bucket."
  [s3-client :- s/Any
   bucket-name :- s/Str]
  (delete-all-objects s3-client bucket-name)
  (abort-all-multipart-uploads s3-client bucket-name)

  (aws/invoke s3-client
              {:op      :DeleteBucket
               :request {:Bucket bucket-name}}))

;---------------------------------------------------------------------------------------------------
(s/defn create-bucket
  "Create an S3 bucket."
  [s3-client :- s/Any
   bucket-name :- s/Str]
  (aws/invoke s3-client
              {:op      :CreateBucket
               :request {:Bucket bucket-name}}))

(s/defn put-object
  "Put an object specified by bucket, key, & filename into S3."
  [s3-client :- s/Any
   bucket :- s/Str
   key :- s/Str
   filename :- s/Str]
  (let [response (aws/invoke s3-client
                             {:op      :PutObject
                              :request {:Bucket bucket
                                        :Key    key
                                        :Body   (io/input-stream filename)}})]
    response))

(s/defn get-object
  "Retrieve an S3 object specified by bucket and key."
  [s3-client :- s/Any
   bucket :- s/Str
   key :- s/Str]
  (let [response (aws/invoke s3-client
                             {:op      :GetObject
                              :request {:Bucket bucket
                                        :Key    key}})]
    (when (:cognitect.anomalies/category response)
      (throw (ex-info "Failed to retrieve file" response)))
    ; Retrieve object before any debug prints or it will close the input stream under `:Body`
    (let [object (with-open [input-stream (:Body response)]
                   (slurp input-stream))]
      ; (spyx-pretty response)
      object)))
