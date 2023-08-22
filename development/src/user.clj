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
   )
  (:import (com.zaxxer.hikari HikariDataSource)
           (org.postgresql.jdbc PgConnection)))
;; TODO: should use interface instead 
(core/start-server!)
#_((add-libs '{metosin/spec-tools {:mvn/version "0.10.5"}})
   (add-libs '{ring/ring-devel {:mvn/version "1.10.0"}})
   (add-libs '{com.taoensso/timbre {:mvn/version "6.2.2"}})
   (add-libs '{buddy/buddy-auth {:mvn/version "3.0.323"}})
   (add-libs '{clj-time/clj-time {:mvn/version "0.15.2"}})
   (add-libs '{clj-http/clj-http {:mvn/version "3.12.3"}})
   (add-libs '{buddy/buddy-hashers {:mvn/version "2.0.167"}})
   )
#_(def db-pool
  (connection/->pool HikariDataSource
                     {:jdbcUrl
                      (connection/jdbc-url {:host "localhost"
                                            :dbtype "postgres"
                                            :dbname "uchat"
                                            :useSSL false})
                      :username "postgres" :password ""}))

(defn get-connection-in-pool
  "get java.sql.connection from db-pool"
  [^HikariDataSource datasource]
  (.getConnection datasource))

#_(def conn (get-connection-in-pool db-pool))

#_(def migration-config
  "this is config for ragtime migration tools."
  {:datastore  (ragtime/sql-database (get-connection-in-pool db-pool))
   :migrations (ragtime/load-resources "migrations")})
(comment
  ;(repl/migrate migration-config)
  ;(repl/rollback migration-config)
  )
;; this is config for next-jdbc and hikari
;; need to be refactor later, ref to db-tools
(def config
  {:uchat/db-connection {:dbtype "postgres"
                        :dbname "uchat"
                        :useSSL "false"}
   :uchat/db-auth {:username "postgres" :password ""}})



