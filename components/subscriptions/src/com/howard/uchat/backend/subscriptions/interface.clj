(ns com.howard.uchat.backend.subscriptions.interface
  (:require [next.jdbc :as jdbc]
            [com.howard.uchat.backend.subscriptions.core :as core]
            )
  )

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
  [opt]
  (core/get-user-team-subscirptions opt))
