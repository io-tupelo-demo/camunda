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

(s/defn shell-exec-and-verify
  [cmd :- s/Str]
  (let [result (misc/shell-cmd cmd)]
    (let [exit (:exit result)
          out  (:out result)]
    (when-not (= 0 exit)
        (prn :shell-exec-and-verify--cmd cmd)
        (prn :shell-exec-and-verify--exit exit)
        (prn :shell-exec-and-verify--out out)
        (throw (ex-info "shell-exec-and-verify: error"
                 (ex-info (vals->map cmd exit out))))))))

(defn handler-02
  [externalTask externalTaskService]
  (prn :finch.camunda-task-02/handler-02--enter)
  ; Get a process variable (vars defined in BPMN file)
  (let [fname-834 (.getVariable externalTask "filename")
        fname-xml (filename-834->xml fname-834)]
    (when true ; debug
      (nl)
      (spyx-pretty :finch.camunda-task-02/handler-02--vars
        (vals->map fname-834 fname-xml))
      (nl))

    (let [c1 (str "emerald x12n-to-xml " fname-834 \space fname-xml)
          c2 (str "ls -ldF " fname-834 " " fname-xml)
          c3 "rm -rf   /tmp/data-xml"
          c4 "mkdir -p /tmp/data-xml"
          c5 (str "mv " fname-xml " /tmp/data-xml")]
      (spyx-pretty (misc/shell-cmd c1))
      (spyx-pretty (misc/shell-cmd c2))
      (spyx-pretty (misc/shell-cmd c3 ))
      (spyx-pretty (misc/shell-cmd c4 ))
      (spyx-pretty (misc/shell-cmd c5 ))
      (let [cmd-import (str/whitespace-collapse
                         (str/join \space
                           ["/usr/bin/mlcp  import"
                            "-host" (config/ip-addr-marklogic) "-port 8000"
                            "-username admin  -password admin"
                            "-input_file_path /tmp/data-xml "]))]
        (spyx cmd-import)
        (spyx-pretty (misc/shell-cmd cmd-import)))
      )

    ; "global" output variable defined on the process, not in BPMN file
    (let [vars {"result" "complete"}]
      (.complete externalTaskService externalTask vars))
    (Thread/sleep 222)
    (prn :finch.camunda-task-02/handler-02--leave)))

(defn create
  "Create and activate the task on the Camunda server."
  []
  (tasks/subscribe-to-topic "topic-02" handler-02))
