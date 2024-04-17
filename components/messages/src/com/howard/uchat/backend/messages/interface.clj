(ns com.howard.uchat.backend.messages.interface
  (:require [com.howard.uchat.backend.messages.database :as database]
            [spec-tools.data-spec :as ds])
  (:import [java.sql Connection]))

(defn create-message
  "create a message
  params
  - tx Connection
  - username string
  - msg string
  - channel-uuid uuid"
  [^Connection tx username msg channel-uuid]
  {:pre [(string? username)
         (string? msg)
         (uuid? channel-uuid)]}
  (database/create-message tx username msg channel-uuid))

(defn get-messages-by-channel
  "select messages by channel.
  TODO: pagination."
  [tx channel-id]
  {:pre [(uuid? channel-id)]}
  (database/get-messages-by-channel tx channel-id))

(defn get-message-by-uuid
  "get message by message-uuid"
  [tx message-id]
  {:pre [(uuid? message-id)]}
  (database/get-message-by-uuid tx message-id))
