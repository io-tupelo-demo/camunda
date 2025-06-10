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

; Maybe needed in BPMN file
; <camunda:executionListener class="org.camunda.qa.MyExecutionListener" event="start" />

(defn handler
  [externalTask externalTaskService]
  (spy :handler--enter)
  ; Put your business logic here

  ; Get a process variable
  (let [item   (.getVariable externalTask "item") ; String
        amount (.getVariable externalTask "amount") ; Integer
        ]

    (println (format "Charging credit card with an amount of '%s' for the item '%s'..."
                     amount item))

    ;try {
    ;     Desktop.getDesktop () .browse (new URI ("https://docs.camunda.org/get-started/quick-start/complete")) ;
    ;     } catch (Exception e) {
    ;                            e.printStackTrace () ;
    ;                            }

    ; Complete the task
    (spyx (.complete externalTaskService externalTask)))
  (spy :handler--enter))

(verify
  (let [client (it-> (ExternalTaskClient/create)
                 (.baseUrl it "http://localhost:8080/engine-rest")
                 (.asyncResponseTimeout it 10000) ; long polling timeout
                 (.build it))
        ]
    (spyxx client)
    ; subscribe to an external task topic as specified in the process
    (it-> client
      (.subscribe it "charge-card")
      (.lockDuration it 1000) ; the default lock duration is 20 seconds, but you can override this
      (.handler it handler)
      (.open it)
      )

    ))

