(ns com.howard.uchat.backend.socket.core
  (:require
   [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]
   [clojure.core.async :as async  :refer [<! <!! >! >!! put! chan go go-loop]]
   [taoensso.timbre    :as timbre]
   [taoensso.sente     :as sente]))

(sente/set-min-log-level! :debug)
(let [;; Serialization format, must use same val for client + server:
      packer :edn ; Default packer, a good choice in most cases
      ]

  (defonce chsk-server
    (sente/make-channel-socket-server!
     (get-sch-adapter) {:packer packer
                        :csrf-token-fn nil
                        :user-id-fn (fn [request]
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

(defn broadcast!
  "test a broadcast functionality."
  [username event]
  (chsk-send! username event))
