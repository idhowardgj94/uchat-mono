(ns com.howard.uchat.backend.socket.interface
    (:require
   [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]
   [clojure.core.async :as async  :refer [<! <!! >! >!! put! chan go go-loop]]
   [taoensso.timbre    :as timbre]
   [taoensso.sente     :as sente]
   [com.howard.uchat.backend.tools.interface :refer [reduce-map-to-kebab-case!]]
   [com.howard.uchat.backend.socket.events :as events]
   [com.howard.uchat.backend.socket.core :as core]))

(def ring-ajax-get-or-ws-handshake #'core/ring-ajax-get-or-ws-handshake)
(def ring-ajax-post #'core/ring-ajax-post)

(defonce router_ (atom nil))
(defn  stop-router! [] (when-let [stop-fn @router_] (stop-fn)))
(comment
  (stop-router!))

(defn start-router! []
  (stop-router!)
  (reset! router_
    (sente/start-server-chsk-router!
      core/ch-chsk events/event-msg-handler)))

(defn create-channel-message-event
  "create a :channel.<channel-id>/message event
  :channel-id channel id
  :event any format of map"
  [channel-id event]
  [(keyword (str "channel." channel-id) "message") (-> (transient {})
                                                       (reduce-map-to-kebab-case! event)
                                                       persistent!)])

(defn broadcast!
  "given a username and a event,
  send it thourgh the chsk.
  params:
  - username string
  - events vector of string"
  [username event]
  {:pre [(string? username) (vector? event)]}
 (core/broadcast! username event))

(comment
  (broadcast! "howardgj94" [:example/test "QQ"])
  ,)
