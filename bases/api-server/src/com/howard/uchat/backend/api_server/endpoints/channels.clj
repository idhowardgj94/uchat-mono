(ns com.howard.uchat.backend.api-server.endpoints.channels
  (:require
   [com.howard.uchat.backend.api-server.middleware :refer [wrap-authentication-guard]]
   [com.howard.uchat.backend.messages.interface :as messages]
   [com.howard.uchat.backend.channels.interface :as channels]
   [com.howard.uchat.backend.api-server.util :as util]
   [com.howard.uchat.backend.socket.interface :as socket]
   [ring.util.response :as response]
   [spec-tools.data-spec :as ds]
   [clojure.spec.alpha :as s]
   [compojure.core :refer [wrap-routes routes defroutes context GET POST]]
   [com.howard.uchat.backend.teams.interface :as teams]
   [com.howard.uchat.backend.subscriptions.interface :as subscriptions]
   [next.jdbc :as jdbc]
   ))


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
                ;; keyword -- :channel.<channel-id>/message
                (socket/broadcast! username (socket/create-channel-message-event
                                             channel-id result))))))
        (util/json-response
         {:status "success"
          :debug result})))))


(def post-create-channels-spec
  (ds/spec
   {:name ::post-create-channels
    :spec {:channel-name string?
           :team-id string?
           :username-list (s/coll-of string? :kind vector?)}}))

(defn- create-subscriptions
  "create subscription (will call after create channels)"
  [conn body channel-id]
  (jdbc/with-transaction [tx conn]
              (let [username-list (:username-list body)]
                (doseq [username username-list]
                  (subscriptions/create-subscription tx channel-id username)))))

(defn post-create-channels
  [request]
  (let [body (-> request :body)
        conn (-> request :db-conn)
        username (-> request :identity :username)]
    (cond
      (not (s/valid? post-create-channels-spec body)) (response/bad-request
                                                       {:message (str "Wrong body palyoad. please check the API docs. msg:"
                                                                      (s/explain-str post-create-channels-spec body))})
      (not (teams/user-belong-to-team? conn username (parse-uuid (-> body :team-id))))
      (response/bad-request
       {:message (str "You don't have right permission to do this operation.")})
      :else
      (jdbc/with-transaction [tx conn]
        (let [channel-id (channels/create-channel-and-insert-users tx body)]
          (future 
            (create-subscriptions conn body channel-id))
          (util/json-response
           {:status "success"
            :channel-id channel-id}))))))

(defroutes channel-routes
  (context "/api/v1/channels" []
           (wrap-routes
            (routes
             (POST "/" [] post-create-channels)
             (POST "/:channel-id/messages" [] post-channels-message)
             (GET "/:channel-id/messages" [] get-channels-message))
            wrap-authentication-guard)))
#_((-> (new-client "idhowardgj94" "howard")
       (tools/get_ "/api/v1/teams"))
   (-> (new-client "idhowardgj94" "howard")
       (tools/post "/api/v1/channels" {:channel-name "hello"
                                       :team-id "d205e510-8dee-4fb5-8d55-4f4bc5174bad"
                                       :username-list ["idhowardgj94" "eva"]}))
   ,)

