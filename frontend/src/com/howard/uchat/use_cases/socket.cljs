(ns com.howard.uchat.use-cases.socket
  (:require
   [clojure.string  :as str]
   [cljs.core.async :as async  :refer [<! >! put! chan]]
   [taoensso.sente  :as sente  :refer [cb-success?]]
   ; [com.howard.uchat.use-cases.direct :as direct]
   ;; Optional, for Transit encoding:
   [taoensso.sente.packers.transit :as sente-transit]
   [re-frame.core :as re-frame])

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

(defn channel-event-handler
  "handle channel event, try to separte event handler by different event context.
  params
  ev-msg
  {:id string
   :?data ???
   :event map
  }
  :id will be the event's name (first element of a vector) sent from server.
  for example, when server send event
  [:channel.channel-id/message {:foo \"bar\"}]
  then id will be :channel.channel-id/message"
  [{:as ev-msg :keys [id]}]
  (let [channel-regex #"channel\.([\w|-]+)/(\w*)"
        channel-match? (re-find channel-regex (str id))]
    (cond
      (some? channel-match?) (let [channel-id (second channel-match?)
                                   event (get channel-match? 2)]
                               (-event-msg-handler (assoc ev-msg
                                                          :id (keyword "channel" event)
                                                          :channel-id channel-id)) true))))

(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [ev-msg]
  (cond
    (some? (channel-event-handler ev-msg)) true))

(defmethod -event-msg-handler
  :channel/message
  [{:as ev-msg :keys [event channel-id]}]
  (js/console.log (clj->js ev-msg))
  (let [[_ message] event]
    (re-frame/dispatch [:new-message channel-id message])))

(defmethod -event-msg-handler
  :default ; Default/fallback case (no other matching handler)
  [{:as ev-msg :keys [event]}]
  (println ev-msg)
  (js/console.log "Unhandled event: " (clj->js event)))

(-> (re-find #"channel\.([\w|-]+)/(\w*)" "channel.6a77e68b-4d47-4520-b0b4-a8421edc1041/message")
    (get 2))
(defonce router_ (atom nil))
(defn  stop-router! [] (when-let [stop-f @router_] (stop-f)))
(defn start-router! []
  (stop-router!)
  (reset! router_
    (sente/start-client-chsk-router!
      ch-chsk event-msg-handler)))
