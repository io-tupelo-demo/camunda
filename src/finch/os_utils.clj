(ns finch.os-utils
  (:use tupelo.core)
  (:require
    [schema.core :as s]
    [tupelo.string :as str]
    ))

(s/defn system-get-property :- s/Str
  "Get a JVM Property string"
  [prop :- s/Str] (System/getProperty prop))

(s/defn os-name-canonical :- s/Str
  "Return the Operating System name in lowercase with no whitespace (e.g. 'macos', 'windows', 'linux')."
  []
  (let [os-str (system-get-property "os.name")
        result (str/lower-case (str/whitespace-remove os-str))]
    result))

(s/defn is-mac? :- s/Bool
  [] (str/contains-str? (os-name-canonical) "macos"))

(s/defn is-windows? :- s/Bool
  [] (str/contains-str? (os-name-canonical) "windows"))

(s/defn is-linux? :- s/Bool
  [] (str/contains-str? (os-name-canonical) "linux"))
