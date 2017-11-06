(ns seazme.common.hbase
  (:require [cbass :refer [new-connection store find-by scan delete pack-un-pack unpack without-ts get-table]]
            [cbass.scan :refer [scan-filter]]
            [perseverance.core :as p]
            [clojure.tools.logging :as log]
            [seazme.common.config :as config])
  (:import org.apache.hadoop.security.UserGroupInformation)
  (:import [org.apache.hadoop.conf Configuration]
           [org.apache.hadoop.hbase.client HConnection HConnectionManager Table Result Scan])
  )

;; minimum config
                                        ;"hbase.client.retries.number" "1"
                                        ;"zookeeper.session.timeout" "1000"
                                        ;"zookeeper.recovery.retry" "1"
                                        ;"hbase.zookeeper.property.clientPort" "2181"
                                        ;"hbase.zookeeper.quorum" "<comma delimited host list>"
                                        ;"hadoop.security.authentication" "kerberos"
                                        ;"hbase.security.authentication" "kerberos"
                                        ;"hbase.master.kerberos.principal" "hbase/_HOST@<host>"
                                        ;"hbase.regionserver.kerberos.principal" "hbase/_HOST@<host>"

;;TODO handle org.apache.hadoop.hbase.TableNotFoundException, org.apache.hadoop.hbase.UnknownScannerException, org.apache.hadoop.hbase.ipc.RemoteWithExtrasException
(defn ^HConnection new-connection* [config]
  (let [configuration (Configuration.)]
    (log/info "(RE)SETTING HBASE connection")
    (doseq [[k v] (-> config :hbase :configuration)]
      (.set configuration k (str v)))
    (UserGroupInformation/setConfiguration configuration)
    (UserGroupInformation/loginUserFromKeytab (-> config :hbase :kerberos :user) (-> config :hbase :kerberos :path))
    (HConnectionManager/createConnection configuration)))

(def con (atom nil))
;;TODO risk of new-connection might pile up
(defn get-conn[]
  (if (nil? @con)
    (reset! con (new-connection* config/config)) ;; (pack-un-pack {:p identity :u identity})
    @con))

(defn ex-wrapper[e]
  (log/error "RESETTING HBASE connection" e)
  (reset! con nil)
  e)

;;TODO use this (def pr-args {:catch [Exception] :ex-wrapper ex-wrapper})

;;TODO: document need for altered scan
;;TODO: using keywords everywhere, especially for row-key, is probably a bad practice but it makes it easy to traverse results ...
(defn hdata->map* [^Result data]
  (when-let [r (.getRow data)]
    (into {} (for [[k vs] (.getNoVersionMap data)]
               [(keyword (String. ^bytes k))
                (into {} (for [[kv vv] vs]
                           [(keyword (String. ^bytes kv))
                            (@unpack vv)]
                           ))]
               ))
    ))

(defn results->maps* [results row-key-fn]
  (for [^Result r results]
    [(row-key-fn (.getRow r))
     (hdata->map* r)]))

(defn scan1* [table & {:keys [row-key-fn limit with-ts? lazy?] :as criteria}]
  (with-open [^Table h-table (get-table (get-conn) table)]
    (let [results (-> (.iterator (.getScanner h-table ^Scan (scan-filter criteria)))
                      iterator-seq)
          row-key-fn (or row-key-fn #(keyword (String. ^bytes %)))
          rmap (results->maps* (if-not limit
                                results
                                (take limit results))
                              row-key-fn)]
      (cond->> rmap
        (not with-ts?) (without-ts)
        (not lazy?) (into [])))));; {} -> []
(defn scan* [& args] (p/retry {} (p/retriable {:catch [Exception] :ex-wrapper ex-wrapper} (apply scan1* args))))

(defn store1* [table row-key family columns] (store (get-conn) table (name row-key) (name family) columns))
(defn store* [& args] (p/retry {} (p/retriable {:catch [Exception] :ex-wrapper ex-wrapper} (apply store1* args))))

(defn find-by1* [table row-key family] (find-by (get-conn) table (name row-key) (name family)))
(defn find-by* [& args] (p/retry {} (p/retriable {:catch [Exception] :ex-wrapper ex-wrapper} (apply find-by1* args))))

(defn status[]
  #_{}
  (p/retry {} (p/retriable {:catch [Exception] :ex-wrapper ex-wrapper} (-> (get-conn) .getAdmin .getClusterStatus)))
  )

;;TODO implement truly lazy scan and/or memoize last id/data for given app id;;  :lazy? true
