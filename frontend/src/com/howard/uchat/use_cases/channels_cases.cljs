(ns com.howard.uchat.use-cases.channels-cases
  (:require
   [re-frame.core :as re-frame]
   [com.howard.uchat.api :as api]
   [cljs.spec.alpha :as s]
   [day8.re-frame.tracing :refer-macros [fn-traced]]))



(re-frame/reg-event-fx
 ::create-channel
 (fn-traced
  [_ [_ payload]]
  (s/assert api/post-generate-channel-spec payload)
  (-> (api/post-generate-channels payload)
      (.then (fn [res]
               (js/console.log "success" (clj->js res))
               (let [channel-uuid (-> res :data :channel-id)]
               (re-frame/dispatch [:routes/navigate [:routes/channels {:uuid channel-uuid
                                                         :channel-type :channel}]]))            
               )))
  {}))
