(ns com.howard.uchat.backend.socket.interface
    (:require
   [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]
   [clojure.core.async :as async  :refer [<! <!! >! >!! put! chan go go-loop]]
   [taoensso.timbre    :as timbre]
   [taoensso.sente     :as sente]))

(sente/set-min-log-level! :debug)
(let [;; Serialization format, must use same val for client + server:
      packer :edn ; Default packer, a good choice in most cases
      ;; (sente-transit/get-transit-packer) ; Needs Transit dep
      ]

  (defonce chsk-server
    (sente/make-channel-socket-server!
     (get-sch-adapter) {:packer packer
                        :csrf-token-fn nil
                        :user-id-fn (fn [request]
                                     
                                      (timbre/info "inside user-id-fn")
                                      (timbre/info request)
                                      (timbre/info (get-in request [:identity :username]))
                                      (get-in request [:identity :username]))
                        })))

(let [{:keys [ch-recv send-fn connected-uids_ private
              ajax-post-fn ajax-get-or-ws-handshake-fn]}
      chsk-server]

  (defonce ring-ajax-post                ajax-post-fn)
  (defonce ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (defonce ch-chsk                       ch-recv) ; ChannelSocket's receive channel
  (defonce chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (defonce connected-uids_               connected-uids_)   ; Watchable, read-only atom
  (defonce conns_                        (:conns_ private)) ; Implementation detail, for debugging!
  )

(add-watch connected-uids_ :connected-uids
           (fn [_ _ old new]
             (when (not= old new)
               (timbre/infof "Connected uids change: %s" new))))

#_(remove-watch connected-uids_ :connected-uids)
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

(defonce router_ (atom nil))
(defn  stop-router! [] (when-let [stop-fn @router_] (stop-fn)))
(comment
  (stop-router!))

(defn start-router! []
  (stop-router!)
  (reset! router_
    (sente/start-server-chsk-router!
      ch-chsk event-msg-handler)))

