;;TODO replace datahubserv with hub or seazmehub, also in clients
(defproject datahubserv "0.8.1"
  :description "DataHub"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [danlentz/clj-uuid "0.1.7"]
                 [ring-logger "0.7.7"]
                 ;;[ch.qos.logback/logback-classic "1.1.3"]
                 ;;[clojusc/friend-oauth2 "0.2.0"]
                 [metosin/compojure-api "1.1.11"]
                 [digest "1.4.4"]
                 [saml20-clj "0.1.3"]
                 ]
  :ring {:handler seazme.hub.main/app}
  :source-paths ["src/main/clojure"]
  :resource-paths ["resources"]
  :uberjar-name "datahubserv.jar"
  :uberwar-name "datahubserv.war"
  :profiles {:dev {:dependencies [[javax.servlet/javax.servlet-api "3.1.0"]] :plugins [[lein-ring "0.12.0"]]}})
