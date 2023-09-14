(ns com.howard.uchat.backend.api-server.middleware
  (:require
   [buddy.auth :refer [authenticated? throw-unauthorized]]
   [com.howard.uchat.backend.database.interface :as database]))

(defn wrap-cors
  [handler]
  (fn [request]
    (let [response (handler request)
          headers (conj (or (:headers response) {})
                        {"Access-Control-Allow-Origin" "*"
                         "Access-Control-Allow-Headers" "*"
                         "Access-Control-Allow-Methods" "*"})]

      (assoc response :headers headers))))

(defn wrap-database
  [handler]
  (fn [request]
    (let [request' (assoc request :db-conn (database/get-pool))]
      (handler request'))))

(defn wrap-authentication-guard
  [handler]
  (fn [request]
    (if-not (authenticated? request)
      (throw-unauthorized)
      (handler request))))
