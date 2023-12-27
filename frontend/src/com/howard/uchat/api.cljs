(ns com.howard.uchat.api
  #_{:clj-kondo/ignore [:unresolved-var]}
  (:require
   [spec-tools.data-spec :as ds]
   [cljs.spec.alpha :as s]
   ["axios$default" :as axios]
   [goog.object :as obj]
   [ajax.core :refer [GET POST] :as ajax]))

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
             #js {:team-uuid team-uuid
                  :other-user other-user})))

(defn get-team-users
  "get team users by teams-id"
  [team-id]
  {:pre [(string? team-id)]}
  (-> axios
      (.get (str endpoint "/api/v1/teams/" team-id "/users"))))

(def post-generate-channel-spec
  (ds/spec
   {:name ::post-generate-channel-spec
    :spec {:channel-name string?
           :team-id string?
           :username-list (s/coll-of string? :kind vector?)}}))

(defn post-generate-channels
  "params:
  :channel-name - string
  :team-id - string
  :username-list - vocter<string>"
  [payload]
  {:pre [(s/assert post-generate-channel-spec payload)]}
  (-> axios
      (.post (str endpoint "/api/v1/channels")
             (clj->js payload))))

(defn register'
  [payload]
  (-> axios
      (.post (str endpoint "/api/v1/register")
             (clj->js payload))))

(defn login
  [payload]
  (-> axios
      (.post (str endpoint "/api/v1/login") (clj->js payload))))

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
                     :team-uuid "bd9a04af-899d-4d61-a169-8dba5dca99d8"} #(print %))
  )

(comment
  (register {:name "client0355"
             :password "123445"
             :email "client0535@gmail.com"
             :username "clie3nt52034"} #(println %)))
