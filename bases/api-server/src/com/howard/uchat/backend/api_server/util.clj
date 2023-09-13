(ns com.howard.uchat.backend.api-server.util
  (:require
   [ring.util.response :as response]))

(defn json-response
  "add response with header content-type: application/json"
  [body]
  (assoc-in (response/response body) [:headers "Content-Type"] "application/json"))
