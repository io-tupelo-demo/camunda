(ns tst.demo.core
  (:use demo.core tupelo.core tupelo.test)
  (:require
    [demo.task-01 :as task-01]
    [demo.task-02 :as task-02]
    ))

(verify
  (task-01/create)
  (task-02/create)
  )
