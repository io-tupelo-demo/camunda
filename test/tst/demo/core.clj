(ns tst.demo.core
  (:use demo.core
        tupelo.core
        tupelo.test)
  (:require
    [tupelo.string :as str]
    )
  (:import
    [org.camunda.bpm.client ExternalTaskClient])
  )

(def ^:dynamic *debug* true)

; Maybe needed in BPMN file
; <camunda:executionListener class="org.camunda.qa.MyExecutionListener" event="start" />

(defn newfile-handler
  [externalTask externalTaskService]
  (when *debug*
    (nl)
    (prn :----------------------------------------------------------------------------------------------------)
    (spy :newfile--enter)
    (nl)
    (spyx externalTask)
    (nl)
    (spyx externalTaskService)
    )

  ; Get a process variable (vars defined in BPMN file)
  (let [bucket       (.getVariable externalTask "bucket")
        key          (.getVariable externalTask "key")

        filename     (str "/" bucket "/" key) ; pretend we copy the file here

        camunda-data (vals->map bucket key)]

    (nl)
    (spyx-pretty camunda-data)
    (nl)

    (when *debug* ; debug info
      (spyx (Thread/currentThread))
      (spyx (.isDaemon (Thread/currentThread)))
      (nl)
      (println "Thread/dumpStack")
      (Thread/dumpStack)
      (nl)
      )

    (let [vars {"filename" filename}] ; output variable defined in BPMN file (string key!)
      ; Complete the task
      (spyx (.complete externalTaskService externalTask vars))))

  (when *debug*
    (spy :newfile--leave)
    (nl))
  )

(defn touchfile-handler
  [externalTask externalTaskService]
  (when *debug*
    (nl)
    (prn :----------------------------------------------------------------------------------------------------)
    (spy :touchfile--enter)
    (nl)
    (spyx externalTask)
    (nl)
    (spyx externalTaskService)
    )

  ; Get a process variable (vars defined in BPMN file)
  (let [bucket       (.getVariable externalTask "bucket")
        key          (.getVariable externalTask "key")
        filename     (.getVariable externalTask "filename")

        camunda-data (vals->map bucket key filename)]

    (nl)
    (spyx-pretty camunda-data)
    (nl)

    (when *debug* ; debug info
      (spyx (Thread/currentThread))
      (spyx (.isDaemon (Thread/currentThread)))
      (nl)
      (println "Thread/dumpStack")
      (Thread/dumpStack)
      (nl))

    (let [vars {"result" "complete"}] ; output variable defined in BPMN file (string key!)
      ; Complete the task
      (spyx (.complete externalTaskService externalTask vars))))

  (when *debug*
    (spy :touchfile--leave)
    (nl)))

; #todo ************************************************************************************
(when true ; #todo ***** enable when camunda server is running *****************************

  (verify
    (let [client-newfile (it-> (ExternalTaskClient/create)
                           (.baseUrl it "http://localhost:8080/engine-rest")
                           (.asyncResponseTimeout it 10000) ; (millis) long polling timeout
                           (.build it))]
      (nl)
      (prn :----------------------------------------------------------------------------------------------------)
      (spyxx client-newfile)

      (let [; subscribe to an external task topic as specified in the process
            subscription (it-> client-newfile
                           (.subscribe it "new-file-found")
                           (.lockDuration it 9999) ; (millis) default is 20 seconds, but can override
                           (.handler it newfile-handler)
                           (.open it))]
        (nl)
        (prn :----------------------------------------------------------------------------------------------------)
        (spyxx subscription)))

    (let [client-touchfile (it-> (ExternalTaskClient/create)
                             (.baseUrl it "http://localhost:8080/engine-rest")
                             (.asyncResponseTimeout it 10000) ; (millis) long polling timeout
                             (.build it))]
      (nl)
      (prn :----------------------------------------------------------------------------------------------------)
      (spyxx client-touchfile)

      (let [; subscribe to an external task topic as specified in the process
            subscription (it-> client-touchfile
                           (.subscribe it "touch-file-topic")
                           (.lockDuration it 9999) ; (millis) default is 20 seconds, but can override
                           (.handler it touchfile-handler)
                           (.open it))]
        (nl)
        (prn :----------------------------------------------------------------------------------------------------)
        (spyxx subscription)))

    ))
