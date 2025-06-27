(ns demo.task-02
  (:use demo.core
        tupelo.core
        tupelo.test)
  (:require
    [tupelo.string :as str]
    )
  (:import
    [org.camunda.bpm.client ExternalTaskClient]))

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
  (let [client-02       (it-> (ExternalTaskClient/create)
                          (.baseUrl it "http://localhost:8080/engine-rest")
                          (.asyncResponseTimeout it 10000) ; (millis) long polling timeout
                          (.build it))
        subscription-02 (it-> client-02
                          (.subscribe it "topic-02")
                          (.lockDuration it 9999) ; (millis) default is 20 seconds, but can override
                          (.handler it handler-02)
                          (.open it))]
    ; subscription is active - no further action needed
    ))


