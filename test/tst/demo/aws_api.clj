(ns tst.demo.aws-api
  (:use demo.aws-api
        tupelo.core
        tupelo.test)
  (:require
    [clojure.java.io :as io]
    [clojure.test]
    [cognitect.aws.client.api :as aws]
    [cognitect.aws.credentials :as credentials]
    [environ.core :as environ]
    [schema.core :as s]
    [tupelo.java-time :as jt]
    [tupelo.string :as str]
    ))

; #todo move to demo.util
(verify
  (with-redefs [system-get-property (const->fn "Windows 1776")]
    (isnt (is-linux?))
    (isnt (is-mac?))
    (is (is-windows?)))
  (with-redefs [system-get-property (const->fn "Mac OS 1776")]
    (isnt (is-linux?))
    (is (is-mac?))
    (isnt (is-windows?)))
  (with-redefs [system-get-property (const->fn "Linux Ultra 1776")]
    (is (is-linux?))
    (isnt (is-mac?))
    (isnt (is-windows?))))

;---------------------------------------------------------------------------------------------------
; #todo #awt clean up to use profiles.clj and environ lib
(def s3-keys
  (if (not (is-linux?))
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
    (not (is-linux?)) (assoc it :endpoint-override {:protocol :http
                                                    :hostname "localhost"
                                                    :port     19000})))
(def s3-client (aws/client s3-client-opts))

(when (is-linux?)
  (verify-focus
    (spyx s3-keys)
    (spyx s3-creds-provider)
    (spyx s3-client-opts)
    (spyx s3-client)

    (let [aws-result (aws/invoke s3-client {:op :ListBuckets})
          buckets    (:Buckets aws-result)]
      (spyx-pretty aws-result)
      (spyx-pretty buckets)
      ; make a bucket
      (let [aws-result (create-bucket s3-client "tst-bucket-808")]
        (spyx-pretty aws-result)
        )))

  )

(when (is-mac?)
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
  )
