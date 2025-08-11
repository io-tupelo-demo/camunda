(ns demo.camunda-exec
      (:use tupelo.core )
      (:require
        [demo.task-01 :as task-01]
        [demo.task-02 :as task-02]
        ))

(prn :demo.camunda-exec)
(defn -main
  [& args]
  (spy :main--enter)
  (task-01/create)
  (task-02/create)
  (spy :main--leave))
