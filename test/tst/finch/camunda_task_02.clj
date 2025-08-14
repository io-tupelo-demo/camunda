(ns tst.finch.camunda-task-02
  (:use finch.camunda-task-02 tupelo.core tupelo.test)
  (:require
    [tupelo.misc :as misc]
    [tupelo.string :as str]
    ))

(verify
  (is= "sample-file-834.xml"
    (filename-834->xml "sample-file.834"))

  (shell-exec-and-verify "doofus")
  )

