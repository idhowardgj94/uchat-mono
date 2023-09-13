(ns com.howard.uchat.backend.subscriptions.database
  (:require [next.jdbc :as jdbc]
            [com.howard.uchat.backend.database.interface :as database :refer [dbfn]]
            
            [com.howard.uchat.backend.database.interface :as db]))

(dbfn get-user-team-direct-subscriptions
  "get user team direct subscriptions"
  [tx username team-uuid]
  (into
   []
   (map #(-> (select-keys % [:channel_uuid :username :other_user :unread :last_message_uuid :created_at :updated_at :name :type])
             (assoc :type "direct")))
   (jdbc/plan
    tx
    ["SELECT teams_users.username AS other_user, subscriptions.* FROM subscriptions RIGHT JOIN
teams_users ON subscriptions.other_user = teams_users.username AND subscriptions.username = ? WHERE team_uuid = ?" username team-uuid])))

(dbfn get-user-team-channel-subscriptions
  "this is a dbfn TODO: docstring need to be optional"
  [tx username team-uuid]
  (into
   []
   (map #(-> (select-keys % [:channel_uuid :username :unread :last_message_uuid :created_at :updated_at :name :type])
             (assoc :type "channel")))
   (jdbc/plan
    tx
    ["SELECT channels.*, subscriptions.* FROM subscriptions RIGHT JOIN
channels ON channels.uuid=subscriptions.channel_uuid and
subscriptions.username = ?
WHERE team_uuid = ?" username team-uuid])))

(dbfn get-team-users
      "TODO: pagination.
  get team uuid.
  "
      [tx team-uuid]
      (into []
            (map #(select-keys % [:username :email :name :id]))
            (database/plan! tx
                            ["SELECT users.* FROM users JOIN teams_users on users.username = teams_users.username where team_uuid = ?" team-uuid])))

(dbfn get-team-channels
      "TODO: pagination
  get team chanenl"
      [tx team-uuid]
      (into []
            (map #(select-keys % [:name :uuid :created_at :updated_at]))
            (database/plan! tx
                            ["SELECT * FROM channels where team_uuid = ?" team-uuid])))



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

(dbfn create-channel
      "create a channel by channel name and uuid."
      [tx name team-uuid]
      (jdbc/execute!
       tx
       ["INSERT INTO channels (name, team-uuid) VALUES (?, ?)" name team-uuid]))

(defn create-subscription
  "create a subscription for user
  TODO: use dbfn"
  ([tx channel-uuid username unread last-message-uuid]
   (jdbc/execute!
    tx
    ["INSERT INTO subscriptions (channel_uuid, username, unread, last_message_uuid) VALUES
(?, ?, ?, ?)" channel-uuid username unread last-message-uuid]))
  ([tx channel-uuid username]
   (create-subscription tx channel-uuid username 0 nil)))

(comment
  "if get subscriptions won't get any of subscriptions, then it should auto generate
  some subscription.")

(dbfn get-channels-by-team-uuid
      "TODO: can't skip docstring"
      [tx team-uuid]
      (jdbc/execute! tx
                     (database/get-pool)
                     ["SELECT * FROM channels where team_uuid=?" team-uuid]))


;; --- channel

#_(defn create-channel
  )
;; TODO: for test.
#_(defn execute!
    "execute qury from current databsource."
    [sql]
    (jdbc/execute!
     (database/get-pool)
     sql))

(comment
  (db/execute! ["select * from channels"])
  (get-team-users #uuid "684062e0-4b68-4458-873e-6bc22ddbd925")
  (get-team-channels #uuid "684062e0-4b68-4458-873e-6bc22ddbd925")
  (get-team-users #uuid "684062e0-4b68-4458-873e-6bc22ddbd925")
  (get-user-team-direct-subscriptions (database/get-pool) "eva" #uuid "684062e0-4b68-4458-873e-6bc22ddbd925")
  (get-user-team-channel-subscriptions (database/get-pool) "eva" #uuid "684062e0-4b68-4458-873e-6bc22ddbd925")
  (get-team-channels  #uuid "684062e0-4b68-4458-873e-6bc22ddbd925")
  (jdbc/execute! (database/get-pool) ["ALTER TABLE subscriptions ADD COLUMN other_user VARCHAR(255);"]))
