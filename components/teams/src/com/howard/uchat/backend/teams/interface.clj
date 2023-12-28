(ns com.howard.uchat.backend.teams.interface
  (:require [com.howard.uchat.backend.teams.database :as db]
            [com.howard.uchat.backend.tools.interface :refer [export-fn]]
            [com.howard.uchat.backend.database.interface :as database]))

(export-fn get-team-users db/get-team-users)

(defn user-belong-to-team?
  "check if a user belong to a team or not,
  return boolean"
  [db-conn username team_uuid]
  (db/is-user-in-team db-conn username team_uuid))


(defn create-team-by-name
  "create a team, given a name
  TODO: name should be unique, or at least public is a key.
  .... do I have public or private in teams?"
  [db-conn name]
  (db/insert-team-by-name db-conn name))

(defn get-teams-user-belong-to
  "get teams that user belong to"
  [db-conn username]
  (db/get-teams-user-belong-to db-conn username))

(defn is-team-exist?
  "check a team exist or not, by team name"
  [db-conn name]
  (> (count (db/get-team-by-name db-conn name)) 0))
  
(defn add-user-to-team
  "add a user to a team, given username and team_uuid"
  [db-conn username team-uuid]
  (db/insert-users-into-team db-conn team-uuid username))

(defn get-team-by-name
  "get tame by team name"
  [db-conn name]
  (db/get-team-by-name db-conn name))

(defn get-teams
  [db-conn]
  (db/get-teams db-conn))
(defn is-user-in-team
  [username team_uuid]
  (db/is-user-in-team username team_uuid))

(comment
  (get-team-by-name (database/get-pool) "public")
  (add-user-to-team (database/get-pool) "idhowardgj94"  #uuid "684062e0-4b68-4458-873e-6bc22ddbd925")
  ,)
