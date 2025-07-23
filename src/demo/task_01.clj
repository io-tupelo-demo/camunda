(ns demo.task-01
  (:use tupelo.core tupelo.test)
  (:require
    [demo.tasks :as tasks]
    [schema.core :as s]
    [tupelo.schema :as tsk]
    ))

(defn handler-01
  [externalTask externalTaskService]
  ; Get a process variable (vars defined in BPMN file)
  (let [bucket       (.getVariable externalTask "bucket")
        key          (.getVariable externalTask "key")

        filename     (str "/" bucket "/" key) ; pretend we copy the file here

        camunda-data (vals->map bucket key)]

    (nl)
    (spyx-pretty :handler-01 camunda-data)
    (nl)

    ; "global" output variable defined on the process, not in BPMN file
    (let [vars {"filename" filename}]
      (.complete externalTaskService externalTask vars)))
  )

(defn create
  "Create and activate the task on the Camunda server."
  []
  (tasks/subscribe-to-topic "topic-01" handler-01)
  )

