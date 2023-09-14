(ns com.howard.uchat.backend.api-server.endpoints.channels
  (:require
   [com.howard.uchat.backend.api-server.middleware :refer [wrap-authentication-guard]]
   [com.howard.uchat.backend.teams.interface :as teams]
   [com.howard.uchat.backend.api-server.util :as util]
   [compojure.core :refer [wrap-routes routes defroutes context GET]]
   [com.howard.uchat.views.channel :as channel]))

(defn post-create-direct-handler
  [request]
  (let [{:keys [username]} (:identity request)
        other-user (-> request
                       :params
                       :other-user)]
    "TODO"))
