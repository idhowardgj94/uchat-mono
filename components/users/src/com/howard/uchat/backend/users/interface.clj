(ns com.howard.uchat.backend.users.interface
  (:require [next.jdbc :as jdbc]
            [next.jdbc.date-time]
            [clj-time.core :as time]
            [com.howard.uchat.backend.database.interface :as db]
            [buddy.hashers :as hashers])
  (:import [java.sql Timestamp]))

(set! *warn-on-reflection* true)

(defn get-user-by-username
  "get user by username"
  [username]
  (first
   (into (list)
         (map #(select-keys % [:id :username :password]))
         (jdbc/plan (db/get-pool) ["SELECT * FROM users where username=?"
                                   username]))))

(defn insert-to-db
  [username password name email]
  (jdbc/execute!
   (db/get-pool)
   ["INSERT INTO users (username, password, name, email) VALUES (?, ?, ?, ?)"
    username, (hashers/derive password) name, email]))

(comment
  jdbc/plan
  transduce
  reduce
  (insert-to-db "howard3" "1234" "howard2" "idhowardgj9444@gmail.com")
  (get-user-by-username "howard3"))
