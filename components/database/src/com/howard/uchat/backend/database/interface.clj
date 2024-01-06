(ns com.howard.uchat.backend.database.interface
  (:require
   [clj-time.core :as time]
   [com.howard.uchat.backend.database.core :as core]
   [potemkin :refer [import-fn]])
  (:import
   [java.sql Timestamp]))

(defmacro dbfn
  {:clj-kondo/lint-as 'clojure.core/defn}
  [name & args]
  (let [doc (if (string? (first args)) (first args) nil)
        params (if (nil? doc) (first args) (second args))
        second-params (if (<= (count params) 1) [] (into [] (drop 1 params)))
        body (if (nil? doc) (drop 1 args) (drop 2 args))
        body-form `((~params
                     ~@body)
                    (~second-params
                     (apply ~name (get-pool) ~second-params)))]
    `(defn ~name
       ~@(if (some? doc)
           `(~doc
             ~@body-form)
           `(~@body-form)))))

(defn current-timestamp
  "get current timestamp"
  []
  (let [t (.getMillis  (time/now))
        timestamp (Timestamp. t)]
    timestamp))

(import-fn core/perform-migrations)
(import-fn core/init-database)
(import-fn core/perform-rollback!)
(import-fn core/get-pool)
(import-fn core/close-database!)
(def mk-migraiton-config
  "gererate a migration config for use for ragtime repl."
  #'core/mk-migration-config)

(comment
  (close-database! (get-pool))
  (print "hello"))
