(ns demo.task-02
  (:use tupelo.core tupelo.test)
  (:require
    [demo.tasks :as tasks]
    [schema.core :as s]
    [tupelo.schema :as tsk]
    ))

(defn handler-02
  [externalTask externalTaskService]
  ; Get a process variable (vars defined in BPMN file)
  (let [bucket       (.getVariable externalTask "bucket")
        key          (.getVariable externalTask "key")
        filename     (.getVariable externalTask "filename")

        camunda-data (vals->map bucket key filename)]

    (nl)
    (spyx-pretty :handler-02 camunda-data)
    (nl)

    ; "global" output variable defined on the process, not in BPMN file
    (let [vars {"result" "complete"}]
      (.complete externalTaskService externalTask vars))))

(defn create
  "Create and activate the task on the Camunda server."
  []
  (tasks/subscribe-to-topic "topic-02" handler-02)
  )
