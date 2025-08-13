(ns demo.camunda-exec
      (:use tupelo.core )
      (:require
        [demo.camunda-task-01 :as task-01]
        [demo.camunda-task-02 :as task-02]
        ))

(def ip-addr-camunda-qa  "10.128.4.224")
(def ^:dynamic *camunda-url* (str "http://" ip-addr-camunda-qa ":8080/engine-rest"))

(prn :demo.camunda-exec)
(defn -main
  [& args]
  (spy :main--enter)
  (task-01/create)
  (task-02/create)
  (spy :main--leave))
