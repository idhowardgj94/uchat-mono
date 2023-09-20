(ns com.howard.uchat.api
  #_{:clj-kondo/ignore [:unresolved-var]}
  (:require
   [com.howard.uchat.db :as db]
   ["axios$default" :as axios]
   [goog.object :as obj]
   [ajax.core :refer [GET POST] :as ajax]
   [re-frame.core :as re-farme]))
;; TODO: migrate ajax to axios

(defonce endpoint "http://localhost:4000")
(defonce token (atom (-> js/localStorage
                         (.getItem "token"))))

(defn axios-response-to-clj
  []
  (-> axios
      (.-interceptors)
      (.-response)
      (.use (fn [config]
              (js->clj config :keywordize-keys true)))))

(defn remove-response-to-clj
  []
  (-> axios
      (.-interceptors)
      (.-response)
      (.clear)))

(defn add-axios-auth
  "add axios request auth header, given token."
  [token]
  (-> axios
      (.-interceptors)
      (.-request)
      (.use (fn [config]
              (obj/set
               (-> config
                   (.-headers))
               "authorization"
               (str "token " token))
              config))))

(defn remove-axios-auth
  "basicly call clear and remove all interceptors.
  in this project, just clear auth."
  []
  (-> axios
      (.-interceptors)
      (.-request)
      (.clear)))

(defn post-message-to-channels
  [username msg channel-uuid]
  (-> axios
      (.post (str endpoint "/api/v1/channels/" channel-uuid "/messages")
             #js {:username username
                  :msg msg})))

(defn get-messages-by-channel-id
  [channel-id]
  (-> axios
      (.get (str endpoint "/api/v1/channels/" channel-id "/messages"))))
(defn get-me
  []
  (-> axios
      (.get (str endpoint "/api/v1/me"))))

(defn post-generate-direct
  [team-uuid other-user]
  (-> axios
      (.post (str endpoint "/api/v1/direct/generate")
             #js {:team_uuid team-uuid
                  :other_user other-user})))

(defn default-warning-handler
  [err]
  (js/console.warn (clj->js err)))

(def json-request
  "This is a default json request setting for uchat."
  {:format :json
   :keywords? true
   :response-format (ajax/json-response-format {:keywords? true})})

(defn register
  "register user to uchat
  params:
  map: :user :email :password :username"
  ([payload handler]
   (register payload handler #(js/console.error (clj->js %))))
  ([payload handler error-handler]
   #_{:clj-kondo/ignore [:unresolved-var]}
   (POST (str endpoint "/api/v1/register") (merge json-request
                                                  {:params payload
                                                   :error-handler error-handler
                                                   :handler handler}))))

(defn login
  "login user to uchat
  params:
  map :username :password"
  ([payload handler]
   (login payload handler #(js/console.error (clj->js %))))
  ([payload handler error-handler]
   #_{:clj-kondo/ignore [:unresolved-var]}
   (POST (str endpoint "/api/v1/login") (merge json-request
                                               {:params payload
                                                :error-handler error-handler
                                                :handler handler}))))

(defn get-subscription
  ([payload handler]
   (get-subscription payload handler #(js/console.warn %)))
  ([payload handler error-handler]
   (let [token @token]
     (GET (str endpoint "/api/v1/subscriptions")
       (merge json-request
              {:params payload
               :headers {"authorization" (str "token " token)}
               :handler handler
               :error-handler error-handler})))))

(defn get-teams
  ([handler error-handler]
   (let [token @token]
     (GET (str endpoint "/api/v1/teams")
       (merge json-request
              {:headers {"authorization" (str "token " token)}
               :handler handler
               :error-handler error-handler}))))
  ([handler]
   (get-teams handler #'default-warning-handler)))

(comment
  (get-subscription {:type "direct"
                     :team_uuid "bd9a04af-899d-4d61-a169-8dba5dca99d8"} #(print %))
  )

(comment
  (register {:name "client0355"
             :password "123445"
             :email "client0535@gmail.com"
             :username "clie3nt52034"} #(println %))
  (login {:username "idhowardgj94"
          :password "Eva831219"}
         #(println %)))
