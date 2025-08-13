(ns demo.tasks
  (:use tupelo.core)
  (:require
    [finch.os-utils :as os]
    [environ.core :as environ]
    [schema.core :as s]
    [tupelo.schema :as tsk]
    )
  (:import
    [org.camunda.bpm.client ExternalTaskClient]))

(def ip-addr-camunda (if (os/is-mac?)
                       "localhost"
                       (environ/env :ip-addr-camunda)))

(def ^:dynamic *camunda-url* (str "http://" ip-addr-camunda ":8080/engine-rest"))
(def ^:dynamic *async-response-timeout-millis* 9999)

; #todo #awt write macro for generic input/output variables
(comment
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
    ))

(s/defn subscribe-to-topic
  "Attach a handler to a topic via a client subscription."
  [topic-str :- s/Str
   handler-fn :- tsk/Fn]
  (let [client (it-> (ExternalTaskClient/create)
                          (.baseUrl it *camunda-url*)
                          (.asyncResponseTimeout it *async-response-timeout-millis*) ; (millis) long polling timeout
                          (.build it))
        subscription (it-> client
                          (.subscribe it topic-str)
                          (.lockDuration it *async-response-timeout-millis*) ; (millis) default is 20 seconds, but can override
                          (.handler it handler-fn)
                          (.open it))]
    ; subscription is active - no further action needed

    (vals->map client subscription) ; return details (not normally needed)
    ))
