(ns demo.aws-api
  (:use tupelo.core)
  (:require
    [cognitect.aws.client.api :as aws]
    [cognitect.aws.credentials :as credentials]
    [schema.core :as s]
    [tupelo.string :as str]
    ))

(s/defn system-get-property :- s/Str
  [prop :- s/Str] (System/getProperty prop))

(s/defn os-name-canonical :- s/Str
  []
  (let [os-str (system-get-property "os.name")
        result (str/lower-case (str/whitespace-remove os-str))]
    result))

(s/defn is-mac? :- s/Bool
  [] (str/contains-str? (os-name-canonical) "macos"))

(s/defn is-windows? :- s/Bool
  [] (str/contains-str? (os-name-canonical) "windows"))

(s/defn is-linux? :- s/Bool
  [] (str/contains-str? (os-name-canonical) "linux"))

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

(defn create-bucket
  [s3-client bucket-name]
  (aws/invoke s3-client
              {:op :CreateBucket
               :request {:Bucket bucket-name}}))

(defn delete-bucket-force
  [s3-client bucket-name]
  (delete-all-objects s3-client bucket-name)
  (abort-all-multipart-uploads s3-client bucket-name)

  (aws/invoke s3-client
              {:op :DeleteBucket
               :request {:Bucket bucket-name}}))

(defn get-bucket-key
  [s3-client bucket key]
  (let [response (aws/invoke s3-client
                             {:op      :GetObject
                              :request {:Bucket bucket
                                        :Key    key}})]
    (when false ; if print response, it will close the input stream under `:Body`
      (spyx-pretty response))

    (if (:cognitect.anomalies/category response)
      (throw (ex-info "Failed to retrieve file" response))
      (with-open [input-stream (:Body response)]
        (slurp input-stream)))
    ))