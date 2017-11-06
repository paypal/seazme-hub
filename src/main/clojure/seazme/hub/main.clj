(ns seazme.hub.main
  (:require [seazme.hub.api :as a]
            [seazme.hub.biz :as service]))

;;https://github.com/derekchiang/Clojure-Watch for credentials
(def app (a/mk-app
          (service/hashed-passwords)
          {:status service/status
           :POST-applications-intake service/POST-applications-intake
           :POST-intake-sessions service/POST-intake-sessions
           :POST-intake-sessions-_-document service/POST-intake-sessions-_-document
           :POST-intake-sessions-_-cancel service/POST-intake-sessions-_-cancel
           :POST-intake-sessions-_-submit service/POST-intake-sessions-_-submit}))
