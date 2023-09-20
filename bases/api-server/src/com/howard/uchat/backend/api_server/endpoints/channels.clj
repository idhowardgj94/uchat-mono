(ns com.howard.uchat.backend.api-server.endpoints.channels
  (:require
   [com.howard.uchat.backend.api-server.middleware :refer [wrap-authentication-guard]]
   [com.howard.uchat.backend.messages.interface :as messages]
   [com.howard.uchat.backend.api-server.util :as util]
   [ring.util.response :as response]
   [next.jdbc :as jdbc]
   [taoensso.timbre :as timbre]
   [com.howard.uchat.backend.database.interface :as database]
   [spec-tools.data-spec :as ds]
   [clojure.spec.alpha :as s]
   [compojure.core :refer [wrap-routes routes defroutes context GET POST]]))


(defn get-channels-message
  [request]
  (let [channel-uuid (-> request :params :channel-id)
        conn (-> request :db-conn)]
    (if (nil? (parse-uuid channel-uuid))
      (response/bad-request {:status "failed"
                             :message "channel_uuid is not a valid uuid."})
      (util/json-response {:status "success"
                           :result (messages/get-messages-by-channel conn (parse-uuid channel-uuid))}))))

(def post-channels-message-spec
  (ds/spec
   {:name ::post-channels-message
    :spec {:username string?
           :msg string?}}))
  
(defn post-channels-message
  [request]
  (let [body (-> request :body)
        conn (-> request :db-conn)
        channel-id (-> request :params :channel-id)]
    (timbre/info (instance? java.sql.Connection conn))
    (if-not (s/valid? post-channels-message-spec body)
      (response/bad-request
       {:message  (str "Wrong body palyoad. please check the API docs. msg:" (s/explain-str post-channels-message body))})
      (let [{:keys [username msg]} body]
        (timbre/info "inside execute.")
        (util/json-response
         {:status "success"
          :debug
          (messages/create-message conn username msg (parse-uuid channel-id))})))))

(defroutes channel-routes
  (context "/api/v1/channels" []
           (wrap-routes
            (routes
             (POST "/:channel-id/messages" [] post-channels-message)
             (GET "/:channel-id/messages" [] get-channels-message))
            wrap-authentication-guard)))
