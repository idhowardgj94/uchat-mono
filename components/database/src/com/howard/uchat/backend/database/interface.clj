(ns com.howard.uchat.backend.database.interface
  (:require
   [clj-time.core :as time]
   [com.howard.uchat.backend.database.core :as core]
   [com.howard.uchat.backend.tools.interface :refer [export-fn]])
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

(export-fn perform-migrations #'core/perform-migrations)
(export-fn init-database #'core/init-database)
(export-fn perform-rollback! #'core/perform-rollback!)
(export-fn get-pool #'core/get-pool)
(export-fn close-database! #'core/close-database!)
(def mk-migraiton-config
  "gererate a migration config for use for ragtime repl."
  #'core/mk-migration-config)

(comment
  (close-database! (get-pool))
  (print "hello"))
