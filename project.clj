(defproject demo "1.0.0-SNAPSHOT"
  ;-----------------------------------------------------------------------------
  ; ***** NOTE *****
  ; See `deps.edn` re 401 error if minimal `project.clj` not present for UHC/MCNA
  ;-----------------------------------------------------------------------------

  :dependencies [
                 [org.clojure/clojure "1.12.1"]

                 [com.cognitect.aws/api "0.8.762"]
                 [com.cognitect.aws/endpoints "871.2.32.15"]
                 [com.cognitect.aws/s3 "871.2.32.2"]
                 [environ "1.2.0"]
                 [jakarta.xml.bind/jakarta.xml.bind-api "4.0.2"]
                 [org.camunda.bpm/camunda-external-task-client "7.23.0"]
                 [org.slf4j/slf4j-simple "1.7.36"]
                 [prismatic/schema "1.4.1"]
                 [tupelo/tupelo "24.12.25"]
                 ]

  :jvm-opts ["-Xmx4g"]
  :main finch.camunda-init
  )

