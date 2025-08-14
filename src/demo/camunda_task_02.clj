(ns demo.camunda-task-02
  (:use tupelo.core tupelo.test)
  (:require
    [finch.tasks :as tasks]
    [schema.core :as s]
    [tupelo.schema :as tsk]
    ))

(defn handler-02
  [externalTask externalTaskService]
  (prn :demo.task-02/handler-02--enter)
  ; Get a process variable (vars defined in BPMN file)
  (let [bucket       (.getVariable externalTask "bucket")
        key          (.getVariable externalTask "key")
        filename     (.getVariable externalTask "filename")

        camunda-data (vals->map bucket key filename)]

    (when false     ; debug
      (nl)
      (spyx-pretty :handler-02 camunda-data)
      (nl))
    (assert (= camunda-data
               {:bucket "buck" :key "sample.txt" :filename "/buck/sample.txt" } ))

    ; "global" output variable defined on the process, not in BPMN file
    (let [vars {"result" "complete"}]
      (.complete externalTaskService externalTask vars))
    (Thread/sleep 222)
    (prn :demo.task-02/handler-02--leave)))

(defn create
  "Create and activate the task on the Camunda server."
  []
  (tasks/subscribe-to-topic "topic-02" handler-02)
  )
