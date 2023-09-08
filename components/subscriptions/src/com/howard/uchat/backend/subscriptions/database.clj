(ns com.howard.uchat.backend.subscriptions.database
  (:require [next.jdbc :as jdbc]
            [com.howard.uchat.backend.users.interface :as user]
            [com.howard.uchat.backend.database.interface :as database :refer [dbfn]]
            [ragtime.next-jdbc :as ragtime]
            [com.howard.uchat.backend.database.interface :as db]))

;; TODO:
;; https://hasura.io/docs/latest/schema/postgres/default-values/created-updated-timestamps/
(dbfn is-user-in-teams
      "predicated that is user in team or not"
      [tx username team_uuid]
      (-> (database/execute! tx ["SELECT count(*) FROM teams_users WHERE team_uuid = ? AND username = ?" team_uuid username])
          first
          :count
          (> 0)))

(dbfn get-teams
      "get teams"
      [tx]
      (into []
             (map #(select-keys % [:uuid :name]))
            (jdbc/plan tx ["SELECT * FROM teams"])))

(dbfn get-teams-user-belong-to
      "get the user's team by username"
      [tx username]
      (into []
            (map #(select-keys % [:team_uuid :name :username :created_at :updated_at]))
            (database/plan! tx
                            ["SELECT * FROM teams JOIN teams_users on teams_users.team_uuid = teams.uuid WHERE
username = ?" username])))

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

(dbfn insert-team-by-name
      "insert team by name"
      [tx name]
      (jdbc/execute! tx
                     ["INSERT INTO teams (name) VALUES (?)" name]))

(defn insert-users-into-team
  "insert some user by username and team-uuid
  TODO: cannot use dbfn because & in pramas"
  [tx team-uuid & usernames]
  (let [stmts (->> usernames
                   (map #(vector % team-uuid))
                   (into []))]
    (jdbc/execute-batch!
     tx
     "INSERT INTO teams_users (username, team_uuid) VALUES
(?, ?)"
     stmts {})))

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


;; TODO: for test.
#_(defn execute!
    "execute qury from current databsource."
    [sql]
    (jdbc/execute!
     (database/get-pool)
     sql))

(comment
  "TODO linter for config"
  (get-teams)
  (get-teams-user-belong-to "eva")
  (get-team-users #uuid "684062e0-4b68-4458-873e-6bc22ddbd925")
  (get-team-channels #uuid "684062e0-4b68-4458-873e-6bc22ddbd925")
  (get-team-users #uuid "684062e0-4b68-4458-873e-6bc22ddbd925")
  (get-user-team-direct-subscriptions (database/get-pool) "eva" #uuid "684062e0-4b68-4458-873e-6bc22ddbd925")
(get-user-team-channel-subscriptions (database/get-pool) "eva" #uuid "684062e0-4b68-4458-873e-6bc22ddbd925")
  (get-team-channels  #uuid "684062e0-4b68-4458-873e-6bc22ddbd925")
  (jdbc/execute! (database/get-pool) ["ALTER TABLE subscriptions ADD COLUMN other_user VARCHAR(255);"])
  )
