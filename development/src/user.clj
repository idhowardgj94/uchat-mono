(ns user
  (:require
   [clojure.tools.deps.alpha.repl :refer [add-libs]]
   [com.howard.uchat.backend.database.interface :as database]
   [integrant.core :as ig]
   [com.howard.uchat.backend.api-server.system :as s]
   [ragtime.repl :as repl]))


; (def system (ig/init (s/read-system "system.edn")))

(comment
  (ig/halt! system)
  (print "hello")
  (add-libs '{com.taoensso/sente {:mvn/version "1.19.2"}})
  (repl/rollback (database/mk-migraiton-config (database/get-pool)))
  (repl/migrate (database/mk-migraiton-config (database/get-pool)))
  
  (add-libs '{potemkin/potemkin {:mvn/version "0.4.6"}})
  ,)

 
