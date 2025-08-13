(ns finch.camunda-task-02
  (:use tupelo.core)
  (:require
    [demo.tasks :as tasks]
    [cognitect.aws.client.api :as aws]
    [cognitect.aws.credentials :as credentials]
    [demo.os-utils :as os]
    [finch.config :as config]
    [schema.core :as s]
    [tupelo.schema :as tsk]
    ))

(defn handler-02
  [externalTask externalTaskService]
  (prn :finch.camunda-task-02/handler-02--enter)
  ; Get a process variable (vars defined in BPMN file)
  (let [fname     (.getVariable externalTask "filename")]
    (when true     ; debug
      (nl)
      (spyx-pretty :finch.camunda-task-02/handler-02--fname fname)
      (nl))

    (spit fname content-str) ; save file content locally

    ; "global" output variable defined on the process, not in BPMN file
    (let [vars {"result" "complete"}]
      (.complete externalTaskService externalTask vars))
    (Thread/sleep 222)
  (prn :finch.camunda-task-02/handler-02--leave)))

(defn create
  "Create and activate the task on the Camunda server."
  []
  (tasks/subscribe-to-topic "topic-02" handler-02))
