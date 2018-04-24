(ns seazme.hub.test
  (:require [ring.util.http-response :refer [ok not-found accepted]]
            [clj-uuid :as uuid]
            [clojure.tools.logging :as log]
            [clj-time.format :as tf]
            [clj-time.core :as tr]
            [clj-time.coerce :as te]
            [digest :as d])
  (:import java.util.Base64))


(defn encode-base64 [to-encode]
  (.encodeToString (Base64/getEncoder) (.getBytes to-encode)))

(def wip "wip: API response may not be fully defined yet")

(defn hashed-passwords[] #{(d/sha-1 (str "Basic " (encode-base64 "open:seazme")))})

(defn status[op headers]
  (log/info op headers)
  (ok
   {:dead-servers "tmp"
    :load-average "tmp"
    :comment (str wip " for " headers)}
   )
  )

;;TODO update description and seq diagrams
(defn POST-applications-intake[op headers description kind bu instance notification_contacts base_url]
  (log/info op headers description kind bu instance notification_contacts base_url)
  (ok {:id (str (uuid/v4)) :comment wip})
  )

(let [counter (atom 0)]
  (defn POST-intake-sessions[op headers app-id command description]
    (log/info op headers app-id command description)
    (swap! counter inc)
    (condp = command
      "scan" (ok {:key (str (uuid/v4)) :expires 0 :command command :comment "simulating 200" :range {:from 0 :to (te/to-long (tr/now))}})
      "update" (if (= 0 (mod @counter 3))
                 (accepted {:comment "simulating 202"})
                 (ok {:key (str (uuid/v4)) :expires 0 :command command :comment wip :range {:from (- (te/to-long (tr/now)) (* 1000 60 10)):to (te/to-long (tr/now))}})
                 )
      (not-found {:comment (str "command:" command " is invalid")})))

  )

(defn POST-intake-sessions-_-document[op headers session-id payload]
  (log/info op headers session-id (count payload))
  (ok {:comment wip})
  )

(defn POST-intake-sessions-_-cancel[op headers session-id]
  (log/info op headers session-id)
  (ok {:comment wip})
  )

(defn POST-intake-sessions-_-submit[op headers session-id count]
  (log/info op headers session-id count)
  (ok {:comment wip})
  )
