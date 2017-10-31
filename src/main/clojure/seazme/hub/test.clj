(ns seazme.hub.test
  (:require [ring.util.http-response :refer [ok]]
            [clojure.tools.logging :as log]))

(def wip "wip: API response may not be fully defined yet")

(defn status[]
  (ok
   {:dead-servers "tmp"
    :load-average "tmp"
    :comment wip}
   )
  )

(defn POST-intake-sessions[session-id payload]
  (log/info session-id (type payload) payload)
  (ok
   {:comment wip}
   )
  )
