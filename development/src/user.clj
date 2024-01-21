(ns user
  (:require
   [clojure.tools.deps.alpha.repl :refer [add-libs]]
   [com.howard.uchat.backend.database.interface :as database]
   [integrant.core :as ig]
   [com.howard.uchat.backend.api-server.system :as s]
   [ragtime.repl :as repl]))


(defonce system (atom nil))

(defn restart-server
  []
  (when (some? @system)
    (ig/halt! @system))
  (reset! system (ig/init (s/read-system "system.edn"))))

#_(restart-server)
(comment
  (use '[clojure.tools.namespace.repl :only (refresh)])
  (refresh)
  (ig/halt! @system)
  (add-libs '{com.taoensso/sente {:mvn/version "1.19.2"}})
  (repl/rollback (database/mk-migraiton-config (database/get-pool)))
  (repl/migrate (database/mk-migraiton-config (database/get-pool)))
  
  (add-libs '{potemkin/potemkin {:mvn/version "0.4.6"}})
  ,)

 
