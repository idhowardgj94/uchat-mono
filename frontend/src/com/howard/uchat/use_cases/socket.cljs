(ns com.howard.uchat.use-cases.socket
   (:require
   [clojure.string  :as str]
   [cljs.core.async :as async  :refer [<! >! put! chan]]
   [taoensso.encore :as encore :refer-macros [have have?]]
   [taoensso.timbre :as timbre :refer-macros []]
   [taoensso.sente  :as sente  :refer [cb-success?]]

   ;; Optional, for Transit encoding:
   [taoensso.sente.packers.transit :as sente-transit])

  (:require-macros
   [cljs.core.async.macros :as asyncm :refer [go go-loop]]))

(defn init-sente
  [token]
  (let [;; Serializtion format, must use same val for client + server:
        packer :edn ; Default packer, a good choice in most cases
      ;; (sente-transit/get-transit-packer) ; Needs Transit dep

        {:keys [chsk ch-recv send-fn state]}
        (sente/make-channel-socket-client!
         "/api/v1/chsk" ; Must match server Ring routing URL
         nil 
         {:type   :ws
          :ajax-opts {:headers
                      {"authorization" (str "token " token)}}
          :headers {"authorization" (str "token " token)}
          :params {"authorization" (str "token " token)}
          :host "localhost"
          :port 4000
          :packer packer})]

    #_{:clj-kondo/ignore [:inline-def]}
    (def chsk       chsk)
    #_{:clj-kondo/ignore [:inline-def]}
    (def ch-chsk    ch-recv) ; ChannelSocket's receive channel
    #_{:clj-kondo/ignore [:inline-def]}
    (def chsk-send! send-fn) ; ChannelSocket's send API fn
    #_{:clj-kondo/ignore [:inline-def]}
    (def chsk-state state)   ; Watchable, read-only atom
    ))


(defmulti -event-msg-handler
  "Multimethod to handle Sente `event-msg`s"
  :id ; Dispatch on event-id
  )

(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [{:as ev-msg :keys [id ?data event]}]
  (-event-msg-handler ev-msg))

(defmethod -event-msg-handler
  :default ; Default/fallback case (no other matching handler)
  [{:as ev-msg :keys [event]}]
  (js/console.log "Unhandled event: " (clj->js event)))

(defonce router_ (atom nil))
(defn  stop-router! [] (when-let [stop-f @router_] (stop-f)))
(defn start-router! []
  (stop-router!)
  (reset! router_
    (sente/start-client-chsk-router!
      ch-chsk event-msg-handler)))
