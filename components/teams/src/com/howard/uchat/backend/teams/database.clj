(ns com.howard.uchat.backend.teams.database
  (:require
   [next.jdbc :as jdbc]
   [com.howard.uchat.backend.database.interface :as db :refer [dbfn]]))


(dbfn is-user-in-team
      "predicated that is user in team or not"
      [tx username team_uuid]
      (-> (jdbc/execute! tx ["SELECT count(*) FROM teams_users WHERE team_uuid = ? AND username = ?" team_uuid username])
          first
          :count
          (> 0)))

(defn get-team-users
  "given a teams id, return users
  params:
  tx - db
  team-id - uuid"
  [tx team-id]
  {:pre [(uuid? team-id)]}
  (-> (into []
            (map #(select-keys % [:team_uuid :id :username :email :name]))
            (jdbc/plan
             tx
             ["SELECT teams_users.team_uuid, u.id, u.username, u.email, u.name FROM teams_users JOIN users u ON u.username = teams_users.username where teams_users.team_uuid = ?" team-id]))))

(comment
  "an valid example"
  (get-team-users (db/get-pool) #uuid "d205e510-8dee-4fb5-8d55-4f4bc5174bad"))

(dbfn insert-team-by-name
      "insert team by name"
      [tx name]
      (jdbc/execute! tx
                   ["INSERT INTO teams (name) VALUES (?)" name]))

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
            (jdbc/plan tx
                      ["SELECT * FROM teams JOIN teams_users on teams_users.team_uuid = teams.uuid WHERE
username = ?" username])))

(defn insert-users-into-team
  "insert some user by username and team-uuid"
  [tx team-uuid & usernames]
  (let [stmts (->> usernames
                   (map #(vector % team-uuid))
                   (into []))]
    (jdbc/execute-batch!
     tx
     "INSERT INTO teams_users (username, team_uuid) VALUES
(?, ?)"
     stmts {})))

(dbfn create-team-by-name
  "give a name, create a team by that name."
  [tx name]
  (jdbc/execute! tx ["INSERT INTO teams (name) VALUES (?)" name]))

(dbfn get-team-by-name
  "get a team by name"
  [tx name]
  (jdbc/execute! tx ["SELECT * FROM teams WHERE name=?" name]))

(comment
  (get-teams)
  (get-team-by-name "public")
  (create-team-by-name "public")
  (get-teams-user-belong-to "eva")
  ,)
