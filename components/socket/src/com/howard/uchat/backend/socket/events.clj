(ns com.howard.uchat.backend.socket.events
  (:require
   [taoensso.timbre    :as timbre]))

;; event msg handler
(defmulti -event-msg-handler
  "Multimethod to handle Sente `event-msg`s"
  :id ; Dispatch on event-id
  )

(defmethod -event-msg-handler
  :default ; Default/fallback case (no other matching handler)
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [id (:identity ring-req)
        username     (:username id)]
    (timbre/debugf "Unhandled event: %s" event)
    (when ?reply-fn
      (?reply-fn {:unmatched-event-as-echoed-from-server event}))))

(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [{:as ev-msg :keys [id ?data event]}]
  ;;(-event-msg-handler ev-msg) ; Handle event-msgs on a single thread
  (future (-event-msg-handler ev-msg)) ; Handle event-msgs on a thread pool
  )

