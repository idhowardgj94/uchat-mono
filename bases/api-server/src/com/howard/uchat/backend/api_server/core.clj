(ns com.howard.uchat.backend.api-server.core
  (:require
   [compojure.core :refer :all]
   [ring.middleware.json :refer [wrap-json-response]]
   [ring.middleware.reload :refer [wrap-reload]]
   [ring.middleware.resource :refer [wrap-resource]]
   [ring.util.response :refer [resource-response]]
   [ring.middleware.defaults :refer :all]
   [org.httpkit.server :as hk-server]))

(defn wrap-cors
  [handler]
  (fn [request]
    (assoc-in (handler request) [:headers "Access-Control-Allow-Origin"] "*")))


(defonce server (atom nil))
(defroutes app-routes
  (GET "/" [] "hello, howard"))

(defn start-server!
  "start a restful api server,
  store it to server"
  []
  (reset! server
          (hk-server/run-server
           (-> #'app-routes
               (wrap-reload)
               (wrap-cors)
               (wrap-resource "public")
               (wrap-defaults api-defaults)
               (wrap-json-response))
           {:join? false :port 4000})))

(defn stop-server!
  "stop restful api server"
  []
  (@server))

(comment
  (start-server!)
  (stop-server!))
