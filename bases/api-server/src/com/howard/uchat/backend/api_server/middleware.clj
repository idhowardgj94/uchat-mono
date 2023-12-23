(ns com.howard.uchat.backend.api-server.middleware
  (:require
   [buddy.auth :refer [authenticated? throw-unauthorized]]
   [buddy.auth.protocols :as proto]
   [com.howard.uchat.backend.database.interface :as database]
   [camel-snake-kebab.core :as cbk]))

(defn wrap-cors
  [handler]
  (fn [request]
    (let [response (handler request)
          headers (conj (or (:headers response) {})
                        {"Access-Control-Allow-Origin" "*"
                         "Access-Control-Allow-Headers" "*"
                         "Access-Control-Allow-Credentials" true
                         "Access-Control-Allow-Methods" "*"})]

      (assoc response :headers headers))))

(defn wrap-database
  ([handler]
   (fn [request]
     (let [request' (assoc request :db-conn (database/get-pool))]
       (handler request'))))
  ([handler db-pool]
   (fn [request]
     (let [request' (assoc request :db-conn db-pool)]
       (handler request')))))

(defn wrap-authentication-guard
  [handler]
  (fn [request]
    (if-not (authenticated? request)
      (throw-unauthorized)
      (handler request))))

(defn- parse-header-or-request
  [token-name request]
  (let [header (some->> (-> request :headers (get "authorization"))
                        (re-find (re-pattern (str "^" token-name " (.+)$")))
                        (second))
        token (some->> (-> request :params :authorization)
                        (re-find (re-pattern (str "^" token-name " (.+)$")))
                        (second))]
    (or header token)))

(defn wrap-token-from-params
  "This is a hack of buddy-auth.
  because we need to forfill use case of websocket,
  so we need the authenticated form either headers or params,
  so write this wraper to deal with."
  [handler backend]
  (fn [request]
    (when backend
      (let [request (assoc request :auth-backend backend)
            auth-data (some->> request
                               (parse-header-or-request "token")
                               (proto/-authenticate backend request))]
        (-> (assoc request :identity auth-data)
            (handler))))))

(declare reduce-vector-to-kebab-case!)
(declare reduce-map-to-kebab-case!)

(defn- reduce-vector-to-kebab-case!
  "a small helper function to reduce vector to kebab case
  new-v must be a ttrasient collection."
  [new-v v]
  (reduce
   (fn [res v]
     (conj! res (if (map? v) 
                    (-> (reduce-map-to-kebab-case! (transient {}) v)
                        persistent!)
                    v)))
            new-v
            v))

(defn- reduce-map-to-kebab-case!
  "a small warper for reduce map key to kebab case.
   new body neet to be transient collection"
  [new-body body]
  (reduce
   (fn [res [k v]]
     (assoc! res
             (cbk/->kebab-case k)
             (if (vector? v)
               (persistent! (reduce-vector-to-kebab-case! (transient []) v))
               v)))
   new-body
   body))
#_(persistent! (reduce-map-to-kebab-case! (transient {}) {:fooBar "foo"
                                                          :hello [{:helloWord "hi"}
                                                                  {:oneMore [{:testAgain "123"}]}]}))

(defn wrap-kebab-case-converter
  "convert response body's key from other case to kebab case."
  [handler]
  (fn [request]
    (let [response (handler request)
          body (response :body)
          new-body (transient {})]
      (assoc response :body (-> new-body
                                (reduce-map-to-kebab-case! body)
                                persistent!)))))

#_(macroexpand '(->> (:result body)
                     (map #(reduce-key-to-kebab-case! res %))))
