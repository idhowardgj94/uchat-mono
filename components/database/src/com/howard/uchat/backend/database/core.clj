(ns com.howard.uchat.backend.database.core
  (:require
   [com.howard.uchat.backend.database.spec :as spec]
   [clojure.spec.alpha :as s]
   [next.jdbc.connection :as connection]
   [ragtime.next-jdbc :as ragtime]
   [ragtime.repl :as repl]
   [taoensso.timbre :as timbre])
  (:import (com.zaxxer.hikari HikariDataSource)))


;; TODO util-tools 
(defmacro check-spec
  "check spec before enter function,
  if failed, use explain-str to explain failed reason"
  [spec params]
  `(nil? (assert (s/valid? ~spec ~params)
          (s/explain-str ~spec ~params))))

(defn- get-connection-in-pool
  "get java.sql.connection from db-pool"
  [^HikariDataSource datasource]
  (.getConnection datasource))

(defn- init-pool
  [options]
  (connection/->pool HikariDataSource options))

(defn mk-migration-config
  "make migration config from hikari datasource
  dbpath default: migrations"
  ([db-pool db-path]
   {:datastore (ragtime/sql-database (get-connection-in-pool db-pool))
    :migrations (ragtime/load-resources db-path)})
  ([db-pool]
   (mk-migration-config db-pool "migrations")))

(defonce ^:private db-state
  (atom {:migration-config nil
         :db-pool nil}))

(defn init-database
  "initialise database, create a connection pool, and execute
  data migration.
  params:
  options: db connection option
  resources-path: db path, default: migrations"
  ([options db-path]
   {:pre [(check-spec spec/database-option-spec options)]}
   (timbre/info "init data base connection pool...")
   (let [pool (init-pool options)
         migration-config (mk-migration-config pool db-path)]
     (swap! db-state assoc
            :migration-config migration-config
            :db-pool pool)
     (timbre/info "perform data migration...")
     (repl/migrate migration-config)))
  ([options]
   (init-database options "migrations")))

(defn get-pool
  "get connection pool, for db"
  []
  (let [db-pool (:db-pool @db-state)]
    (if (nil? db-pool)
      (timbre/error "can't get connection pool, try to setup database first")
        db-pool)))

(comment
  @db-state
  (repl/rollback (:migration-config @db-state))
  (init-database {:jdbcUrl
                  (connection/jdbc-url {:host "localhost"
                                        :dbtype "postgres"
                                        :dbname "uchat"
                                        :useSSL false})
                  :username "postgres" :password "postgres"})
  )
