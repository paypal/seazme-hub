(ns seazme.common.config
  (:require
   [clojure.tools.logging :as log]
   [clojure.java.io :as io]
   [clojure.edn :as edn]
   [environ.core :refer [env]]))

(defn read-config [env-name file-name]
  (let [path (or (env-name env) file-name)]
    (if (.exists (io/file path))
      (edn/read-string (slurp path))
      (do (log/warn "missing file:" file-name)
          {}))))

(def config (read-config :config-file "config.edn"))
