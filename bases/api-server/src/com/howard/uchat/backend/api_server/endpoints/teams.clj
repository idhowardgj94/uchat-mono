(ns com.howard.uchat.backend.api-server.endpoints.teams
  (:require
   [com.howard.uchat.backend.api-server.middleware :refer [wrap-authentication-guard]]
   [com.howard.uchat.backend.teams.interface :as teams]
   [com.howard.uchat.backend.api-server.util :as util]
   [taoensso.timbre :as timbre]
   [compojure.core :refer [wrap-routes routes defroutes context GET]]))

(defn get-teams-handler
  [request]
  (timbre/info "inside get teams-handler")
  (let [{:keys [username]} (:identity request)
        db-conn (:db-conn request)]
    (util/json-response {:status "success"
                         :result (teams/get-teams-user-belong-to  db-conn username)})))

(defn get-team-users-handler
  [request]
  (timbre/info "/teams/:team-id/users")
  (let [params (-> request :params)
        team-id (:team-id params)
        db-conn (:db-conn request)]
    (util/json-response {:status "success"
                         :data (teams/get-team-users db-conn (parse-uuid team-id))})))

(defroutes teams-routes
  (context "/api/v1" []
           (wrap-routes
            (routes
             (GET "/teams/:team-id/users" [] get-team-users-handler)
             (GET "/teams" [] get-teams-handler))
            wrap-authentication-guard)))

#_(macroexpand '(defroutes teams-routes
  (context "/api/v1" []
           (wrap-routes
            (routes
             (GET "/teams" [] get-teams-handler))
            wrap-authentication-guard))))

