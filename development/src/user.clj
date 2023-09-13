(ns user
  (:require
   [com.howard.uchat.backend.api-server.core :as core] 
   [clojure.tools.deps.alpha.repl :refer [add-libs]]
   [ragtime.next-jdbc :as ragtime]
   [ragtime.repl :as repl]
   [next.jdbc :as jdbc]
   [next.jdbc.sql :as sql]
   [next.jdbc.connection :as connection]
   [clj-time.core :as time]
   [com.howard.uchat.backend.database.interface :as database]
   )
  (:import (com.zaxxer.hikari HikariDataSource)
           (org.postgresql.jdbc PgConnection)))

(database/init-database {:jdbcUrl
                         (connection/jdbc-url {:host "localhost"
                                               :dbtype "postgres"
                                               :dbname "uchat"
                                               :useSSL false})
                         :username "postgres" :password "postgres"})

(comment
  (print "hello")
  (repl/rollback (database/mk-migraiton-config (database/get-pool)))
  (repl/migrate (database/mk-migraiton-config (database/get-pool)))
  ,)
;; TODO: should use interface instead 
(core/start-server!)

