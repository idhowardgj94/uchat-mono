(ns com.howard.uchat.backend.channels.database
  (:require [next.jdbc :as jdbc]
            [com.howard.uchat.backend.database.interface :as db]))

(defn create-direct-channel
  "crate a direct channel, with two user in it.
  return: channel uuid"
  [conn team_uuid]
  (let [channel (jdbc/execute-one!
                 conn
                 ["INSERT INTO channels (name, team_uuid, type) VALUES (?, ?, ?)" (random-uuid) team_uuid "direct"] {:return-keys true})
        uuid (-> channel
                 :channels/uuid)]
    uuid))
(comment
  (jdbc/with-transaction [tx (db/get-pool)]
    (create-direct-channel tx #uuid "d205e510-8dee-4fb5-8d55-4f4bc5174bad")))

(defn insert-users-into-channel
  "insert users into channel by username and channel uuid"
  [tx cuuid & users]
  (let [stmts (->> users
                   (map #(vector % cuuid))
                   (into []))]
    (jdbc/execute-batch!
     tx
     "INSERT INTO channels_users (username, channel_uuid) VALUES
(?, ?)"
     stmts {})))

(comment
  (jdbc/execute! (db/get-pool)
                 ["ALTER TABLE channels ADD COLUMN type VARCHAR(255)"]))
  
