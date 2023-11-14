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
 
(defn create-channel
  "create a channel by team_uuid, channel name, return channel uuid"
  [conn team_uuid name]
  (let [channel (jdbc/execute-one!
                 conn
                 ["INSERT INTO channels (name, team_uuid, type) VALUES (?, ?, ?)" name team_uuid "channel"] {:return-keys true})
        uuid (-> channel
                 :channels/uuid)]
    uuid))

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

(defn get-cahnnel-users-by-channel-uuid
  [conn channel-uuid]
  (into []
        (map #(select-keys % [:name :username :channel_uuid]))
        (jdbc/plan
         conn
         ["SELECT * FROM channels_users
JOIN users on users.username=channels_users.username WHERE channel_uuid = ?" channel-uuid])))

(comment
  (jdbc/with-transaction [tx (db/get-pool)]
    (create-direct-channel tx #uuid "d205e510-8dee-4fb5-8d55-4f4bc5174bad"))
  ;(get-cahnnels-user-by-channel-uuid (db/get-pool) #uuid "0acb1a34-c7d0-4e17-bd51-543298d5f0d1")
  (jdbc/execute! (db/get-pool)
                 ["ALTER TABLE channels ADD COLUMN type VARCHAR(255)"]))
