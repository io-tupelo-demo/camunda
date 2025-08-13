(ns tst.demo.debug
  (:use tupelo.core tupelo.test)
  (:require
    [clojure.java.shell :as shell]
    [tupelo.misc :as misc]
    [tupelo.string :as str]
    ))

(verify
  (let [result (misc/shell-cmd "date")]
    (spyx-pretty result)

    ))

