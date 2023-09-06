(ns com.howard.uchat.backend.subscriptions.core
  (:require [next.jdbc :as jdbc]
            [com.howard.uchat.backend.users.interface :as user]
            [com.howard.uchat.backend.database.interface :as database]
            [com.howard.uchat.backend.users.interface :as users]
            [ragtime.next-jdbc :as ragtime]))
;; TODO:
;; https://hasura.io/docs/latest/schema/postgres/default-values/created-updated-timestamps/
(defn get-teams
  "get teams"
  []
  (into (list)
        (map #(select-keys % [:id :name]))
        (jdbc/plan (database/get-pool) ["SELECT * FROM teams"])))

(declare execute!)
(defn insert-team-by-name
  "insert team by name"
  [name]
  (jdbc/execute!
   (database/get-pool)
   ["INSERT INTO teams (name) VALUES (?)" name]))

(defn insert-users-into-team
  "insert some user by username and team-uuid"
  [team-uuid & usernames]
  (let [stmts (->> usernames
                   (map #(vector % team-uuid))
                   (into []))]
    (jdbc/execute-batch!
     (database/get-pool)
     "INSERT INTO teams_users (username, team_uuid) VALUES
(?, ?)"
     stmts {})))

(defn insert-users-into-channel
  "insert users into channel by username and channel uuid"
  [cuuid & users]
  (let [stmts (->> users
                   (map #(vector % cuuid))
                   (into []))]
    (jdbc/execute-batch!
     (database/get-pool)
     "INSERT INTO channels_users (username, channel_uuid) VALUES
(?, ?)"
     stmts {})))

(defn create-channel
  "create a channel by channel name and uuid."
  [name]
  (jdbc/execute!
   (database/get-pool)
   ["INSERT INTO channels (name) VALUES (?)" name]))

(defn create-subscription
  "create a subscription for user"
  ([channel-uuid username unread last-message-uuid]
   (jdbc/execute!
    (database/get-pool)
    ["INSERT INTO subscriptions (channel_uuid, username, unread, last_message_uuid) VALUES
(?, ?, ?, ?)" channel-uuid username unread last-message-uuid]))
  ([channel-uuid username]
   (create-subscription channel-uuid username 0 nil)))

;; TODO: for test.
#_(defn execute!
  "execute qury from current databsource."
  [sql]
  (jdbc/execute!
   (database/get-pool)
   sql))


(comment
  (execute! ["CREATE TRIGGER updated_at_subscriptions BEFORE UPDATE ON subscriptions FOR EACH ROW EXECUTE PROCEDURE updated_at_column();"])
  (execute! ["ALTER TABLE subscriptions ALTER COLUMN updated_at SET DEFAULT now()"])
  (execute! ["ALTER TABLE subscriptions ADD COLUMN last_message_uuid uuid"])
  (execute! ["ALTER TABLE subscriptions ALTER COLUMN username type VARCHAR(255)"])
  (def team-id (:teams/id (first (execute! ["SELECT * FROM teams;"]))))
  (execute! ["ALTER TABLE teams rename id TO uuid"])
  (execute! ["ALTER TABLE subscriptions ALTER COLUMN channel_uuid TYPE uuid using gen_random_uuid()"])
  (execute! ["ALTER TABLE teams_users rename user_id TO username;"])
  (execute! ["ALTER TABLE teams_users ALTER COLUMN username TYPE VARCHAR(255)"])
  (execute! ["CREATE UNIQUE INDEX username_idx ON users (username);"])
  (users/insert-to-db "eva" "1234" "EvaWang" "evawang@tsmc.com")
  (execute! ["SELECT * FROM users"])
  (create-channel "global")
  (insert-team-by-name "tchat")
  (insert-team-by-name "tdrive"))
