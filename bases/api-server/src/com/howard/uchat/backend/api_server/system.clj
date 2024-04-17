(ns com.howard.uchat.backend.api-server.system
  "bootstrap whole system using intergrant."
  (:require
   [clojure.java.io :as io]
   [com.howard.uchat.backend.socket.interface :as socket]
   [next.jdbc.connection :as connection]
   [com.howard.uchat.backend.database.interface :as database]
   [com.howard.uchat.backend.api-server.core :as api-server] 
   [integrant.core :as ig]))

(defn read-system
  "read system from edn file, this system file need to put in resource folder."
  [file]
  (-> (io/resource file)
      slurp
      ig/read-string))

(defmethod ig/init-key :db/jdbc [_  {:keys [jdbc-url username password]}]
  (let [db (database/init-database {:jdbcUrl
                                    (connection/jdbc-url jdbc-url)
                                    :username username
                                    :password password})]
    db))

(defmethod ig/halt-key! :db/jdbc [_ db-pool]
  (database/close-database! db-pool))

(defmethod ig/init-key :server/restful [_ {:keys [port db-pool default-team?]}]
  (when default-team?
      (api-server/setup-default-teams))
  (api-server/start-resful-server! db-pool port))

(defmethod ig/halt-key! :server/restful [_ server]
  (server))

(defmethod ig/init-key :server/websocket [_ run?]
  (if run?
    (socket/start-router!)
    nil))

(defmethod ig/halt-key! :server/websocket [_ server]
  (when (some? server)
    (socket/stop-router!)))

(defmethod ig/init-key :db/migrations [_ {:keys [run? db-path db-pool rollback?]}]
  (when run?
    (database/perform-migrations db-pool db-path))
  {:rollback? rollback?
   :db-pool db-pool
   :db-path db-path})

(defmethod ig/halt-key! :db/migrations [_ {:keys [rollback? db-pool db-path]}]
  (when rollback?
    (database/perform-rollback! db-pool db-path)))
