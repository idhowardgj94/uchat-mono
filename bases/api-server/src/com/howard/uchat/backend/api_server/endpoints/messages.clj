(ns com.howard.uchat.backend.api-server.endpoints.messages
  (:require
   [com.howard.uchat.backend.api-server.middleware :refer [wrap-authentication-guard]]
   [com.howard.uchat.backend.messages.interface :as messages]
   [com.howard.uchat.backend.api-server.util :as util]
   [compojure.core :refer [wrap-routes routes defroutes context GET]]
   [taoensso.timbre :as timbre]))

(defn message-handler
  [request]
  (let [channel-uuid (-> request :route-params :channel-uuid)
        username (-> request :identity :username)
        db-conn (-> request :db-conn)]
    (timbre/info (-> request))
    (timbre/info channel-uuid)
    (util/json-response {:status "success"
                         :result (messages/get-messages-by-channel db-conn (parse-uuid channel-uuid))})))

(defroutes messages-routes
  (context "/api/v1" []
           (wrap-routes
            (routes
             (GET "/messages/:channel-uuid" [] message-handler))
            wrap-authentication-guard)))
