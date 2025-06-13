(ns tst.demo.core
  (:use demo.core
        tupelo.core
        tupelo.test)
  (:require
    [tupelo.string :as str]
    )
  (:import
    [java.net URI]
    [java.awt Desktop]
    [org.camunda.bpm.client ExternalTaskClient])
  )

(def ^:dynamic *debug* true)

; Maybe needed in BPMN file
; <camunda:executionListener class="org.camunda.qa.MyExecutionListener" event="start" />

(defn handler
  [externalTask externalTaskService]
  (when *debug*
    (nl)
    (prn :----------------------------------------------------------------------------------------------------)
    (spy :handler--enter))

  ; Get a process variable
  (let [item   (.getVariable externalTask "item") ; String
        amount (.getVariable externalTask "amount") ; Integer
        ]

    (println (format "    Charging credit card with an amount of '%s' for the item '%s'..."
                     amount item))

    (when *debug*     ; debug info
      (nl)
      (spyx (Thread/currentThread))
      (spyx (.isDaemon (Thread/currentThread)))
      (nl)
      (println "Thread/dumpStack")
      (Thread/dumpStack))

    ; Complete the task
    (.complete externalTaskService externalTask))

  (when *debug*
    (spy :handler--leave)
    (nl))
  )

(comment
  (verify
    (let [client (it-> (ExternalTaskClient/create)
                   (.baseUrl it "http://localhost:8080/engine-rest")
                   (.asyncResponseTimeout it 10000) ; long polling timeout (millis)
                   (.build it))]
      (nl)
      (prn :----------------------------------------------------------------------------------------------------)
      (spyxx client)
      (let [; subscribe to an external task topic as specified in the process
            subscription (it-> client
                           (.subscribe it "charge-card")
                           (.lockDuration it 9999) ; (millis) default is 20 seconds, but can override
                           (.handler it handler)
                           (.open it))]
        (nl)
        (prn :----------------------------------------------------------------------------------------------------)
        (spyxx subscription)

        ))))

(verify
  (let [client (it-> (ExternalTaskClient/create)
                 (.baseUrl it "http://localhost:8080/engine-rest")
                 (.asyncResponseTimeout it 10000) ; (millis) long polling timeout
                 (.build it))]
    (nl)
    (prn :----------------------------------------------------------------------------------------------------)
    (spyxx client)
    (let [; subscribe to an external task topic as specified in the process
          subscription (it-> client
                         (.subscribe it "download-file-from-s3")
                         (.lockDuration it 1000) ; (millis) default is 20 seconds, but can override
                         (.handler it handler)
                         (.open it))]
      (nl)
      (prn :----------------------------------------------------------------------------------------------------)
      (spyxx subscription)

      )))
