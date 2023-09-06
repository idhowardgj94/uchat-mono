(ns com.howard.uchat.backend.database.interface
  (:require [com.howard.uchat.backend.database.core :as core]
            [clj-time.core :as time])
  (:import [java.sql Timestamp]))

(defn current-timestamp
  "get current timestamp"
  []
  (let [t (.getMillis  (time/now))
        timestamp (Timestamp. t)]
    timestamp))

(def init-database
  "initialise database, create a connection pool, and execute
  data migration.
  params:
  options: db connection option
  resources-path: db path, default: migrations"
  #'core/init-database)

(defn get-pool
  "get connection pool, for db"
  []
  (core/get-pool))

(def mk-migraiton-config
  "gererate a migration config for use for ragtime repl."
  #'core/mk-migration-config)

(comment
  (print "hello")
  ,)
