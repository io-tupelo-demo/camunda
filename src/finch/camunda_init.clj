(ns finch.camunda-init
  (:use tupelo.core)
  (:require
    [cognitect.aws.client.api :as aws]
    [finch.aws-api :as aws-api]
    [finch.os-utils :as os]
    [finch.camunda-task-01 :as task-01]
    [finch.camunda-task-02 :as task-02]
    [finch.config :as config]
    ))

(defn -main
  [& args]
  (prn :finch.camunda-init--enter)

  (prn :finch.camunda-init--uploading-test-834-file--begin)
  (let [key   "HT007992-001_20220112002237_HT000004-002-100005084.834"
        fname (str "data-834/" key)]
    (spyx-pretty (vals->map key fname))
    (aws-api/put-object config/s3-client config/bucket-name key fname))
  (prn :finch.camunda-init--uploading-test-834-file--end)

  (spy :finch.camunda-init--create-camunda-handlers)
  (task-01/create)
  (task-02/create)

  (spy :finch.camunda-init--leave)
  )
