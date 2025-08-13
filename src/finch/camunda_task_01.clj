(ns finch.camunda-task-01
  (:use tupelo.core
        tupelo.test)
  (:require
    [demo.tasks :as tasks]
    [schema.core :as s]
    [tupelo.schema :as tsk]
    ))

(defn handler-01
  [externalTask externalTaskService]
  (prn :demo.task-01/handler-01--enter)
  ; Get a process variable (vars defined in BPMN file)
  (let [bucket       (.getVariable externalTask "bucket")
        key          (.getVariable externalTask "key")

        fname        (str "/" bucket "/" key) ; pretend we copy the file here

        camunda-data (vals->map bucket key)]

    (when false     ; debug
      (nl)
      (spyx-pretty :handler-01 camunda-data)
      (nl))
    (assert (= camunda-data {:bucket "buck" :key "sample.txt"}))

    ; "global" output variable defined on the process, not in BPMN file
    (let [vars {"filename" fname}] ; save value `fname` to global variable "filename"
      (.complete externalTaskService externalTask vars)))
  (Thread/sleep 111)
  (prn :demo.task-01/handler-01--leave))

(defn create
  "Create and activate the task on the Camunda server."
  []
  (tasks/subscribe-to-topic "topic-01" handler-01)
  )

