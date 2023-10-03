(ns com.howard.uchat.backend.api-server.endpoints.channels
  (:require
   [com.howard.uchat.backend.api-server.middleware :refer [wrap-authentication-guard]]
   [com.howard.uchat.backend.messages.interface :as messages]
   [com.howard.uchat.backend.channels.interface :as channels]
   [com.howard.uchat.backend.api-server.util :as util]
   [ring.util.response :as response]
   [taoensso.timbre :as timbre]
   [spec-tools.data-spec :as ds]
   [clojure.spec.alpha :as s]
   [com.howard.uchat.backend.socket.interface :as socket]
   [compojure.core :refer [wrap-routes routes defroutes context GET POST]]))


(defn ns-map->simple-map
  "covert ns map to simple map,
  params:
  - map: a map want to removed ns
  "
  [m]
  {:pre [(map? m)]}
  (->> m
       (map (fn [[key value]]
              (let [key-str (str key)
                    simple-key-str (re-find #"/(.+)" key-str)]
                (if (nil? simple-key-str)
                  [key value]
                  [(keyword (second simple-key-str)) value]))))
       (into {})))

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
        name (-> request :identity :name)
        channel-id (-> request :params :channel-id)]
    (timbre/info (instance? java.sql.Connection conn))
    (if-not (s/valid? post-channels-message-spec body)
      (response/bad-request
       {:message  (str "Wrong body palyoad. please check the API docs. msg:" (s/explain-str post-channels-message body))})
      (let [{:keys [username msg]} body
            result (assoc (-> (messages/create-message conn username msg (parse-uuid channel-id))
                              (ns-map->simple-map))
                          :username username
                          :name name)]
        (future
          (let [channel-users (channels/get-cahnnel-users-by-channel-uuid conn channel-id)]
            (doseq [channel-user channel-users]
              (let [username (:username channel-user)]
                (socket/broadcast! username [(keyword (str "channel." channel-id) "message")
                                             result])))))
        (util/json-response
         {:status "success"
          :debug result})))))

(defroutes channel-routes
  (context "/api/v1/channels" []
           (wrap-routes
            (routes
             (POST "/:channel-id/messages" [] post-channels-message)
             (GET "/:channel-id/messages" [] get-channels-message))
            wrap-authentication-guard)))
