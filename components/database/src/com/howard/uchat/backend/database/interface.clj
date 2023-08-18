(ns com.howard.uchat.backend.database.interface
  (:require [com.howard.uchat.backend.database.core :as core]))

(def init-database
  "initialise database, create a connection pool, and execute
  data migration.
  params:
  options: db connection option
  resources-path: db path, default: migrations"
  #'core/init-database)

(def get-pool
  "get connection pool, for db"
  #'core/get-pool)


(comment
  ,)
