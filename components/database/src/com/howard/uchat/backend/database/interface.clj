(ns com.howard.uchat.backend.database.interface
  (:require [com.howard.uchat.backend.database.core :as core]
            [clj-time.core :as time]
            [next.jdbc :as jdbc])
  (:import [java.sql Timestamp]))


(defmacro dbfn
  {:clj-kondo/lint-as 'clojure.core/defn}
  [name & args]
  (let [doc (if (string? (first args)) (first args) nil)
        params (if (nil? doc) (first args) (second args))
        second-params (if (<= (count params) 1) [] (into [] (drop 1 params)))
        body (if (nil? doc) (drop 1 args) (drop 2 args))]
    `(defn ~name
         ~doc
         (~params
         ~@body)
         (~second-params
          (apply ~name (get-pool) ~second-params)))))

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
