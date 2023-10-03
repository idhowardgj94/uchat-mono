(ns com.howard.uchat.backend.channels.interface
  (:require [com.howard.uchat.backend.channels.database :as channels]
            [com.howard.uchat.backend.database.interface :as db]
            [next.jdbc :as jdbc]))

(defn create-direct-and-insert-users
  "crate a direct channels and insert users into it.
  params:
  conn: db connection.
  user: one user that inside the direct channel.
  other-user: the other user inside the direct channel."
  [conn team_uuid user other-user]
  (let [channel-uuid (channels/create-direct-channel conn team_uuid)]
    (if (= user other-user)
      (channels/insert-users-into-channel conn channel-uuid user)
      (channels/insert-users-into-channel conn channel-uuid user other-user))
    channel-uuid))

(defn get-cahnnel-users-by-channel-uuid
  [conn channel-uuid]
  (channels/get-cahnnel-users-by-channel-uuid conn (parse-uuid channel-uuid)))

(comment
  (jdbc/with-transaction [tx (db/get-pool)]
    (create-direct-and-insert-users tx #uuid "d205e510-8dee-4fb5-8d55-4f4bc5174bad" "eva" nil))
  )
