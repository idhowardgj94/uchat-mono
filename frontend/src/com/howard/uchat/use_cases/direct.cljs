(ns com.howard.uchat.use-cases.direct
  "this namespace contains use-cases about direct channel."
  (:require
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [com.howard.uchat.db :as db]
   [com.howard.uchat.use-cases.core-cases :as core-cases]
   [com.howard.uchat.api :as api]
   [re-frame.core :as re-frame]
   ))

(re-frame/reg-fx
 ::get-messages
 (fn-traced [channel-id]
            (-> (api/get-messages-by-channel-id channel-id)
                (.then (fn [{:keys [data]}]
                         (let [messages (:result data)]
                           (js/console.log "inside success")
                           (re-frame/dispatch [::core-cases/assoc-in-db [:current-channel :messages] messages])))))))
(re-frame/reg-event-fx
 ::current-channel
 (fn-traced
  [{:keys [db]} [_ type channel-uuid]]
  (let [channels (case type
                   :direct (:direct-subscriptions db)
                   :channel (:channel-subscriptions db))]

    {:db (assoc db :current-channel (->> channels
                                         (some #(when (= (:channel_uuid %) channel-uuid)
                                                  %))))
     :fx [[::get-messages channel-uuid]]
     })))

(re-frame/reg-event-db
 ::clear-message-box
 (fn-traced
  [db [_ channel-uuid]]
  (assoc-in db [:message-box (keyword channel-uuid)] "")))

(re-frame/reg-event-fx
 ::send-message
 (fn-traced
  [{:keys [db]} [_ msg]]
  (let [{user :user
         channel :current-channel} db
        username (:username user)
        channel-uuid (:channel_uuid channel)]
    (-> (api/post-message-to-channels username msg channel-uuid)
        (.then (fn []
                 (re-frame/dispatch [::clear-message-box channel-uuid])
                 ))))
  {}))


