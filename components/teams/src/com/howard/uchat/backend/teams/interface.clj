(ns com.howard.uchat.backend.teams.interface
  (:require [com.howard.uchat.backend.teams.database :as db]
            [com.howard.uchat.backend.database.interface :as database]))

(defn user-belong-to-team?
  "check if a user belong to a team or not,
  return boolean"
  [username team_uuid]
  (db/is-user-in-team username team_uuid))


(defn create-team-by-name
  "create a team, given a name
  TODO: name should be unique, or at least public is a keyword."
  [name]
  (db/insert-team-by-name name))

(defn get-teams-user-belong-to
  "get teams that user belong to"
  [username]
  (db/get-teams-user-belong-to username))

(defn is-team-exist?
  "check a team exist or not, by team name"
  [name]
  (db/get-team-by-name name))
  
(defn add-user-to-team
  "add a user to a team, given username and team_uuid"
  [username team-uuid]
  (db/insert-users-into-team (database/get-pool) team-uuid username))

(defn get-team-by-name
  "get tame by team name"
  [name]
  (db/get-team-by-name name))

(defn is-user-in-team
  [username team_uuid]
  (db/is-user-in-team username team_uuid))
