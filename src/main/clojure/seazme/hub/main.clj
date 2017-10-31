(ns seazme.hub.main
  (:require [seazme.hub.api.handler :as h]
            [seazme.hub.test :as service]))

(org.apache.log4j.BasicConfigurator/configure)

(def app (h/mk-app {:status service/status
                    :POST-intake-sessions service/POST-intake-sessions}))
