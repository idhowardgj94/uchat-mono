(ns com.howard.uchat.backend.api-server.endpoints.teams
  (:require
   [com.howard.uchat.backend.api-server.middleware :refer [wrap-authentication-guard]]
   [com.howard.uchat.backend.teams.interface :as teams]
   [com.howard.uchat.backend.api-server.util :as util]
   [compojure.core :refer [wrap-routes routes defroutes context GET]]))

(defn get-teams-handler
  [request]
  (let [{:keys [username]} (:identity request)
        db-conn (:db-conn request)]
    (util/json-response {:status "success"
                         :result (teams/get-teams-user-belong-to  db-conn username)})))

(defroutes teams-routes
  (context "/api/v1" []
           (wrap-routes
            (routes
             (GET "/teams" [] get-teams-handler))
            wrap-authentication-guard)))

#_(macroexpand '(defroutes teams-routes
  (context "/api/v1" []
           (wrap-routes
            (routes
             (GET "/teams" [] get-teams-handler))
            wrap-authentication-guard))))

