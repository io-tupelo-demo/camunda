(ns demo.core
  (:use tupelo.core)
  (:require
    [schema.core :as s]
    ))

(defn -main
  [& args]
  (spy :main--enter)
  (spy :main--leave))
