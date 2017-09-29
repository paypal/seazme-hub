(ns seazme.hub.api.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [clj-uuid :as uuid]
            [ring.logger :as logger]
            ))

(s/defschema Total {:uuid String (s/optional-key :total) Long :comment String})

;;TODO define , :uuid #"[0-9]+"

(def drtbi "Dummy response, to be implemented")

(def handler
  (api
   {:swagger
    {:ui "/"
     :spec "/swagger.json"
     :data {:info {:title "Agora Data Hub APIs"
                   :description "TBD"}
            :tags [{:name :access :description "SSO based provisioning"}
                   {:name :intake :description "high level data intake"}
                   {:name :data :description "low level data read only access"}
                   {:name :analytics :description "analytics" }
                   ]}}}

   (context "/access" []
            :tags [:access]
            (PUT "/user" []
                 :return {:uuid String :comment String}
                 :query-params [comment :- String, sourceBussinesUnit :- String, sourceType :- String, sourceInstance :- String]
                 :summary "access API key for user with SSO enabled browser, how API is passed TBD"
                 (ok {:uuid (str (uuid/v4)) :comment drtbi}))

            (GET "/user" []
                 :return {:comment String}
                 :summary "returns the list of already provisioned API keys"
                 (ok {:uuid (str (uuid/v4)) :comment drtbi}))

            (DELETE "/user" []
                    :return {:comment String}
                    :summary "unregister API key"
                    (ok {:uuid "ABC" :comment drtbi}))
            )

   (context "/intake" []
            :tags [:intake]

            (PUT "/begin" []
                 :return {:trxid String :comment String}
                 :query-params [comment :- String, incremental :- Boolean]
                 :summary "begin data intake transaction, might contain multiple uploads "
                 (ok {:trxid (str (uuid/v4)) :comment drtbi}))
            ;;TODO explicit expiration

            (POST "/insert/:uuid" [uuid]
                  :return {:comment String}
                  :summary "post of an insert of atomic piece of data (e.g. Page in Confluence)"
                  (ok {:comment drtbi}))
            (POST "/commit/:uuid" [uuid]
                  :return {:comment String}
                  :summary "post a final commit"
                  (ok {:comment drtbi}))

            (POST "/rollback/:uuid" [uuid]
                  :return {:comment String}
                  :summary "post a rollback"
                  (ok {:comment drtbi}))
            )

   (context "/data" []
            :tags [:data]
            (GET "/tbd" []
                 :return {:comment String}
                 :summary "TBD"
                 (ok {:comment drtbi}))
            )

   (context "/analytics" []
            :tags [:analytics]
            (GET "/tbd" []
                 :summary "TBD"
                 (ok {:comment drtbi}))
            )
   ))

(def app (-> handler logger/wrap-with-logger))
