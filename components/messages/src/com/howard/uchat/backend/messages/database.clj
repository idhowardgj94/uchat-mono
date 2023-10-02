(ns com.howard.uchat.backend.messages.database
  (:require
   [com.howard.uchat.backend.database.interface :as db]
   [next.jdbc :as jdbc]
   )
  (:import [java.sql Connection]))

(require '[com.howard.uchat.backend.database.interface :as database])
(require '[next.jdbc :as jdbc])


(defn create-message
  "create a message, given username, msg channel_uuid
  params
  - tx
  - username
  - msg
  - channel_uuid"
  [tx username msg channel-uuid]
  (jdbc/execute-one! tx
                     ["INSERT INTO messages (username, msg, channel_uuid) VALUES (?, ?, ?)"
                      username msg channel-uuid] {:return-keys true}))

(defn get-messages-by-channel
  "select messages by channel.
  TODO: pagination."
  [tx channel-id]
  (into []
        (map #(select-keys % [:uuid :channel_uuid :username :msg :created_at :updated_at :name]))
        (jdbc/plan tx
                   ["SELECT messages.*,  users.name FROM messages JOIN users on users.username = messages.username WHERE channel_uuid = ?" channel-id])))

(defn get-message-by-uuid
  [tx message-id]
  (into []
        (map #(select-keys % [:uuid :channel_uuid :username :msg :created_at :updated_at :name]))
        (jdbc/plan tx
                   ["SELECT messages.*,  users.name FROM messages JOIN users on users.username = messages.username WHERE messages.uuid = ?" message-id])))

(comment
  (create-message (db/get-pool) "ttt" "hello, world" (parse-uuid "21adabdc-f5d6-4348-9ac9-e9d1be7263b3"))
  (get-messages-by-channel (db/get-pool) (parse-uuid "21adabdc-f5d6-4348-9ac9-e9d1be7263b3"))
  ,)
