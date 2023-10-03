(ns user
  (:require
   [com.howard.uchat.backend.api-server.core :as core] 
   [clojure.tools.deps.alpha.repl :refer [add-libs]]
   [ragtime.repl :as repl]
   [next.jdbc.connection :as connection]
   [com.howard.uchat.backend.database.interface :as database]
   ))

(database/init-database {:jdbcUrl
                         (connection/jdbc-url {:host "localhost"
                                               :dbtype "postgres"
                                               :dbname "uchat"
                                               :useSSL false})
                         :username "postgres" :password "postgres"})

(comment
  (print "hello")
  (add-libs '{com.taoensso/sente {:mvn/version "1.19.2"}})
  (repl/rollback (database/mk-migraiton-config (database/get-pool)))
  (repl/migrate (database/mk-migraiton-config (database/get-pool)))
  ,)
;; TODO: should use interface instead 
(core/start-server!)

