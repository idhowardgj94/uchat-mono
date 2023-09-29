(ns com.howard.uchat.backend.users.interface
  (:require [next.jdbc :as jdbc]
            [next.jdbc.date-time]
            [com.howard.uchat.backend.database.interface :as db :refer [get-pool]]
            [buddy.hashers :as hashers])
  (:import [java.sql Timestamp]))

(set! *warn-on-reflection* true)

(defn get-user-by-username
  "get user by username"
  [db-conn username]
  (first
   (into (list)
         (map #(select-keys % [:id :username :password :name :email]))
         (jdbc/plan db-conn ["SELECT * FROM users where username=?"
                                   username]))))

(defn insert-to-db
  [db-conn username password name email]
  (jdbc/execute!
   db-conn
   ["INSERT INTO users (username, password, name, email) VALUES (?, ?, ?, ?)"
    username, (hashers/derive password) name, email]))

(comment
  jdbc/plan
  transduce
  reduce
  (insert-to-db (get-pool) "howard3" "1234" "howard2" "idhowardgj9444@gmail.com")
  (get-user-by-username (get-pool) "howard3"))
