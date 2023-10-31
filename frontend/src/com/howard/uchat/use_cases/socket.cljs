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

(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [{:as ev-msg :keys [id ?data event]}]
  (let [channel-regex #"channel\.([\w|-]+)/(\w*)"
        channel-match? (re-find channel-regex (str id))]
    (cond
      (some? channel-match?) (let [channel-id (second channel-match?)
                                   event (get channel-match? 2)]
                               ;; channel.<channel-id>/<event> -> channel/<event>
                               (-event-msg-handler (assoc ev-msg
                                                          :id (keyword "channel" event)
                                                          :channel-id channel-id)))
      :else (-event-msg-handler ev-msg))))

(defmethod -event-msg-handler
  :channel/message
  [{:as ev-msg :keys [event channel-id]}]
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
