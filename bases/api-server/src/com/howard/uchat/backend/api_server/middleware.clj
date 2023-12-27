(ns com.howard.uchat.backend.api-server.middleware
  (:require
   [buddy.auth :refer [authenticated? throw-unauthorized]]
   [buddy.auth.protocols :as proto]
   [taoensso.timbre :as timbre]
   [com.howard.uchat.backend.tools.interface :refer [reduce-map-to-kebab-case!]]
   [com.howard.uchat.backend.database.interface :as database]
   ))

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


(defn wrap-kebab-case-converter
  "convert response body's key from other case to kebab case."
  [handler]
  (fn [request]
    (let [response (handler request)
          body (:body response)
          new-body (transient {})]
      (if (map? body)
        (assoc response :body (-> new-body
                                  (reduce-map-to-kebab-case! body)
                                  persistent!))
        response))))

#_(macroexpand '(->> (:result body)
                     (map #(reduce-key-to-kebab-case! res %))))
