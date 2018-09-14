(ns seazme.hub.api
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [clj-time.coerce :as co]
            [ring.logger :as logger]
            [saml20-clj.routes :as sr]
            [ring.middleware.session :refer :all]
            [ring.middleware.params :refer :all]
            [clojure.string :as str]
            [digest :as d]))

;;(s/defschema Total {:id String (s/optional-key :total) Long :comment String})
(s/defschema UpdateRange {:from Long :to Long})

;;TODO convert to "(resource" https://github.com/metosin/compojure-api-examples/blob/master/src/compojure/api/examples/handler.clj vs https://github.com/metosin/compojure-api/blob/master/examples/resources/src/example/handler.clj

;;TODO define , :id #"[0-9]+"
;;TODO https://stackoverflow.com/questions/46288109/how-to-stream-a-large-csv-response-from-a-compojure-api-so-that-the-whole-respon
;;TODO WARN Not all child routes satisfy compojure.api.routing/Routing. {:path nil, :method nil}, invalid child routes:

(def wip "wip: API response may not be fully defined yet")

(def wip-return {:comment String s/Keyword s/Any})

(defmacro mk-datahub-handler [cm op & args] `(fn [~'req] (-> ~cm ~op (apply ~op (select-keys (~'req :headers) ["apikey" "x-real-ip"]) ~@args nil))))

;; In ring middleware, "The root cause here is that in Compojure, the routing tree is an application where the middleware are applied in-place - there is no good way to delay the computation to see if the route will eventually match.", hence manual check. `reroute-middleware` might work better.
(defn wrap-auth[sso-config hashed-passwords handler]
  (fn [request]
    (let [uri (request :uri)
          nop-fn #(handler request)
          sso-fn #(if (-> request :session :saml nil?) (sr/redirect-to-saml "/") (nop-fn))
          sso-or-nop-fn #(if (nil? sso-config) (nop-fn) (sso-fn))
          basic-auth-fn #(let [auth-header (get (:headers request) "authorization")]
                           (cond
                             (nil? auth-header) (-> (unauthorized) (header "WWW-Authenticate" "Basic realm=\"whatever\""))
                             (contains? hashed-passwords (d/sha-1 auth-header)) (nop-fn)
                             :else (unauthorized {})))]
      (cond
        (str/starts-with? uri "/v1/datahub/applications/apikey") (sso-or-nop-fn)
        (str/starts-with? uri "/saml") (nop-fn)
        (str/starts-with? uri "/v1") (basic-auth-fn)
        :else (sso-or-nop-fn)))))

;consider instead of comment
;;"errors": {
;;           "id": "missing-required-key"
;;           }


(defn handler[sso-config cm]
  (api
   {:swagger
    {:ui "/"
     :spec "/swagger.json"
     :data {:info {:title "DataHub API"
                   :description "please see docs/DESIGN.org for details"}
            :tags [{:name :access :description "application provisioning: registration/status/deactivation"}
                   {:name :intake :description "data intake process"}
                   {:name :data-read :description "raw and processed data reading"}
                   {:name :analytics :description "analytics results access"}
                   {:name :system :description "system status, stats and meta info"}]
            :securityDefinitions {:BasicAuth
                                  {:type "basic"}}}}}

   (when sso-config (sr/saml-routes sso-config))

   (context "/v1" []
            (context "/datahub" []
                     (GET "/applications/apikey" {session :session}
                          :tags [:access]
                          :query-params [name :- String, contacts :- String, description :- String]
                          ;;contacts is structures list of email,SMS,Slack,etc
                          :return {:apikey String :comment String}
                          :summary "Generates API key (which could replace Basic Auth in feature) after completing SSO SAML 2.0 based authetication."
                          (mk-datahub-handler cm :GET-applications-apikey session))
                     )
            )
   ;;TODO difference between [:r :- String] and {:r String}
   (context "/v1" [:as rr]
            :header-params [apikey :- String]
            (context "/datahub" []
                     (POST "/applications/intake" []
                           :tags [:access :intake]
                           :return {:id String :comment String}
                           :query-params [description :- String, kind :- String, bu :- String, instance :- String, notification_contacts :- String, base_url :- String]
                           :summary "intake application registration async request"
                           (mk-datahub-handler cm :POST-applications-intake description kind bu instance notification_contacts base_url))
                     #_(POST "/applications/data-read" []
                           :tags [:access :data-read]
                           :return wip-return
                           :query-params [description :- String email :- String]
                           :summary "data-read application registration async request"
                           (ok {:comment wip}))
                     (POST "/applications/analytics" []
                           :tags [:access :analytics]
                           :return wip-return
                           :query-params [description :- String email :- String]
                           :summary "analytics application registration async request"
                           (ok {:comment wip}))
                     (GET "/applications/:id" [id]
                          :tags [:access]
                          :return {:request_status String :expires Long :comment String}
                          :summary "application registration status"
                          (ok {:request_status "accepted" :expires 0 :comment wip}))
                     (GET "/applications" []
                          :tags [:access]
                          :return {:ids [String] :comment String}
                          :summary "available applications"
                          (ok {:ids [] :comment wip}))
                     (POST "/applications/:id/deactivate" [id]
                           :tags [:access]
                           :return wip-return
                           :summary "application deactivation"
                           (ok {:comment wip}))
                     (GET "/status" []
                          :tags [:system]
                          :return wip-return
                          :summary "current system status report"
                          (mk-datahub-handler cm :status))
                     (POST "/intake-sessions" []
                           :tags [:intake]
                           :return {:key String :expires Long :command String :comment String :range {:from Long :to Long}}
                           :query-params [app-id :- String command :- String description :- String]
                           :summary "document intake session begin, like SQL's \"BEGIN TRANSACTION\""
                           (mk-datahub-handler cm :POST-intake-sessions app-id command description))
                     (POST "/intake-sessions/:key/document" [key]
                           :tags [:intake]
                           :body [payload s/Any]
                           :return wip-return
                           :summary "document (e.g. page in Confluence) or any other atomic piece of data store"
                           (mk-datahub-handler cm :POST-intake-sessions-_-document key payload))
                     (POST "/intake-sessions/:key/cancel" [key]
                           :tags [:intake]
                           :return wip-return
                           :summary "session cancellation declaration, like SQL's \"ROLLBACK TRANSACTION\""
                           (mk-datahub-handler cm :POST-intake-sessions-_-cancel key))
                     (POST "/intake-sessions/:key/submit" [key]
                           :tags [:intake]
                           :return wip-return
                           :query-params [count :- Long]
                           :summary "sucesfull session end declaration, like SQL's \"COMMIT TRANSACTION\""
                           (mk-datahub-handler cm :POST-intake-sessions-_-submit key count))
                     (GET "/analytics-dummy" []
                          :tags [:analytics]
                          :return wip-return
                          :summary "To be developed. (dummy get exists in order to show up this context in swagger ui.)"
                          (ok {:comment wip}))
                     #_(GET "/dummy" []
                          :tags [:data-read]
                          :return wip-return
                          :summary "To be developed. (dummy get exists in order to show up this context in swagger ui.)"
                          (ok {:comment wip}))
                     ))))

(defn mk-app[sso-config hashed-passwords callbacks-map]
  (->> (handler sso-config callbacks-map) (wrap-auth sso-config hashed-passwords) wrap-params wrap-session logger/wrap-with-logger))
