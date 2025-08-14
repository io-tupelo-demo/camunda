(ns finch.camunda-task-02
  (:use tupelo.core)
  (:require
    [demo.tasks :as tasks]
    [finch.aws-api :as aws-api]
    [finch.config :as config]
    [schema.core :as s]
    [tupelo.misc :as misc]
    [tupelo.string :as str]))

(s/defn filename-834->xml :- s/Str
  "Modify 834 filename for XML conversion output"
  [fname-834 :- s/Str]
  (it-> fname-834
              (str/replace it "." "-")
              (str it ".xml")))

(defn handler-02
  [externalTask externalTaskService]
  (prn :finch.camunda-task-02/handler-02--enter)
  ; Get a process variable (vars defined in BPMN file)
  (let [fname     (.getVariable externalTask "filename")
        fname-xml (it-> fname
                    (str/replace it "." "-")
                    (str it ".xml"))
        ]
    (when true     ; debug
      (nl)
      (spyx-pretty :finch.camunda-task-02/handler-02--vars
        fname)
      (nl))

    (let [cmd-str (str "ls -ldF " fname)
          result  (misc/shell-cmd cmd-str)]
      (spyx-pretty result))

    ; "global" output variable defined on the process, not in BPMN file
    (let [vars {"result" "complete"}]
      (.complete externalTaskService externalTask vars))
    (Thread/sleep 222)
  (prn :finch.camunda-task-02/handler-02--leave)))

(defn create
  "Create and activate the task on the Camunda server."
  []
  (tasks/subscribe-to-topic "topic-02" handler-02))
