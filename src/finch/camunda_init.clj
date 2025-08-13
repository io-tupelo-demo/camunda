(ns finch.camunda-init
      (:use tupelo.core )
      (:require
        [finch.camunda-task-01 :as task-01]
        [finch.camunda-task-02 :as task-02]
        ))

(prn :finch.camunda-init)
(defn -main
  [& args]
  (spy :main--enter)
  (task-01/create)
  (task-02/create)
  (spy :main--leave))
