(ns com.howard.uchat.backend.users.interface
  (:require [next.jdbc :as jdbc]
            [next.jdbc.date-time]
            [clj-time.core :as time]
            [com.howard.uchat.backend.database.interface :as db]
            [buddy.hashers :as hashers]
            )
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
  (let [t (.getMillis  (time/now))
        timestamp (Timestamp. t)]
    (jdbc/execute!
     (db/get-pool)
     ["INSERT INTO users (username, password, name, email, created_on, updated_at) VALUES (?, ?, ?, ?, ?, ?)"
      username, (hashers/derive password) name, email timestamp timestamp])))

(comment
  jdbc/plan
  transduce
  reduce
  (insert-to-db "howard3" "1234" "howard2" "idhowardgj9444@gmail.com")
  (get-user-by-username "howard3")
  ) 
