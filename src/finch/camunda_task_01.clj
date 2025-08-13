(ns finch.camunda-task-01
  (:use tupelo.core)
  (:require
    [demo.tasks :as tasks]
    [cognitect.aws.client.api :as aws]
    [cognitect.aws.credentials :as credentials]
    [finch.aws-api :as aws-api]
    [finch.config :as config]
    [schema.core :as s]
    [tupelo.schema :as tsk]
    ))

(defn handler-01
  [externalTask externalTaskService]
  (prn :finch.camunda-task-01/handler-01--enter)
  ; Get a process variable (vars defined in BPMN file)
  (let [bucket       (.getVariable externalTask "bucket")
        key          (.getVariable externalTask "key")

        content-str (aws-api/get-object->str config/s3-client bucket-name key-name)
        fname        (str "/tmp/" key)]
    (when true     ; debug
      (nl)
      (spyx-pretty :finch.camunda-task-01/handler-01--fname fname)
      (nl))

    (spit fname content-str) ; save file content locally

    ; "global" output variable defined on the process, not in BPMN file
    (let [vars {"filename" fname}] ; save value `fname` to global variable "filename"
      (.complete externalTaskService externalTask vars)))
  (Thread/sleep 111)
  (prn :finch.camunda-task-01/handler-01--leave))

(defn create
  "Create and activate the task on the Camunda server."
  []
  (tasks/subscribe-to-topic "topic-01" handler-01))
