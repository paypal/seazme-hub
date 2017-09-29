(defproject datahubserv "0.1.0"
  :description "DataHub"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [danlentz/clj-uuid "0.1.7"]
                 [ring-logger "0.7.7"]
                 [metosin/compojure-api "1.1.11"]]
  :ring {:handler seazme.hub.api.handler/app}
  :source-paths ["src/main/clojure"]
  :uberjar-name "datahubserv.jar"
  :uberwar-name "datahubserv.war"
  :profiles {:dev {:dependencies [[javax.servlet/javax.servlet-api "3.1.0"]] :plugins [[lein-ring "0.12.0"]]}})
