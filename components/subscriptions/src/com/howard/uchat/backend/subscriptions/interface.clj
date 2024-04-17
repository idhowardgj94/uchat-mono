(ns com.howard.uchat.backend.subscriptions.interface
  (:require
   [com.howard.uchat.backend.tools.interface :refer [export-fn]]
   [com.howard.uchat.backend.subscriptions.database :as database]
   [com.howard.uchat.backend.subscriptions.core :as core]))

(set! *warn-on-reflection* true)

(defn get-user-team-subscriptions
  "
  TODO: spec
  get user's subscription
  recived a map and return result
  opt req:
  - type: :direct or :channel
  - username
  - team-uuid"
  [db-conn opt]
  (core/get-user-team-subscirptions db-conn opt))

(export-fn create-subscription database/create-subscription)

(defn create-direct-subscriptions
  "
  create subscriptions for user
  please give username and other_username, channel_type
  params:
  - tx
  - channel-uuid
  - username
  - other-username
  "
  [tx channel-uuid username other-username]
  (core/create-subscription tx channel-uuid username other-username "direct")
  (when (not= username other-username)
    (core/create-subscription tx channel-uuid other-username username "direct")))
