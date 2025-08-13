(ns tst.demo.core-debug
  (:use tupelo.core tupelo.test)
  (:import
    [org.camunda.bpm.client ExternalTaskClient]))

;*****************************************************************************
; ***** Saving the original version with lots of debug printouts         *****
;*****************************************************************************
(comment

  (def ^:dynamic *debug* false)

  (defn handler-01
    [externalTask externalTaskService]
    (spy :newfile--enter)
    (when *debug*
      (nl)
      (prn :----------------------------------------------------------------------------------------------------)
      (nl)
      (spyx externalTask)
      (nl)
      (spyx externalTaskService))

    ; Get a process variable (vars defined in BPMN file)
    (let [bucket       (.getVariable externalTask "bucket")
          key          (.getVariable externalTask "key")

          filename     (str "/" bucket "/" key) ; pretend we copy the file here

          camunda-data (vals->map bucket key)]

      (nl)
      (spyx-pretty :handler-01 camunda-data)
      (nl)

      (when *debug* ; debug info
        (spyx (Thread/currentThread))
        (spyx (.isDaemon (Thread/currentThread)))
        (nl)
        (println "Thread/dumpStack")
        (Thread/dumpStack)
        (nl))

      ; "global" output variable defined on the process, not in BPMN file
      (let [vars {"filename" filename}]
        (.complete externalTaskService externalTask vars)))

    (spy :newfile--leave)
    )


  ; #todo ************************************************************************************
  (when true ; #todo ***** enable when camunda server is running *****************************

    (verify
      (let [client-newfile (it-> (ExternalTaskClient/create)
                             (.baseUrl it "http://localhost:8080/engine-rest")
                             (.asyncResponseTimeout it 10000) ; (millis) long polling timeout
                             (.build it))]
        (when *debug*
          (nl)
          (prn :----------------------------------------------------------------------------------------------------)
          (spyxx client-newfile))

        (let [; subscribe to an external task topic as specified in the process
              subscription (it-> client-newfile
                             (.subscribe it "topic-01")
                             (.lockDuration it 9999) ; (millis) default is 20 seconds, but can override
                             (.handler it handler-01)
                             (.open it))]
          (when *debug*
            (nl)
            (prn :----------------------------------------------------------------------------------------------------)
            (spyxx subscription))))))

  )
