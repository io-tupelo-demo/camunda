(ns demo.task-01
  (:use demo.core
        tupelo.core
        tupelo.test)
  (:require
    [tupelo.string :as str]
    )
  (:import
    [org.camunda.bpm.client ExternalTaskClient]))

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
  (let [client-01       (it-> (ExternalTaskClient/create)
                          (.baseUrl it "http://localhost:8080/engine-rest")
                          (.asyncResponseTimeout it 10000) ; (millis) long polling timeout
                          (.build it))
        subscription-01 (it-> client-01
                          (.subscribe it "topic-01")
                          (.lockDuration it 9999) ; (millis) default is 20 seconds, but can override
                          (.handler it handler-01)
                          (.open it))]
    ; subscription is active - no further action needed
    ))

