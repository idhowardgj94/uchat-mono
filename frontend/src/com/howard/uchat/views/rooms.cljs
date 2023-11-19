(ns com.howard.uchat.views.rooms
  (:require
   [com.howard.uchat.components.room-components :refer
    [room-header message-contents message-date-line message-card message-intro]]
   [com.howard.uchat.components.message-box :refer [message-box]]
   [re-frame.core :as re-frame]
   [com.howard.uchat.db :as db]
   [com.howard.uchat.use-cases.direct :as direct-event]
   ["moment" :as moment]
   ))
(defn- add-timeline-string-to-messages
  "add timeline string to messages if need.
  ; TODO: it should have cchance to refactor it to use map"
  [messages]
  (if (= (count messages) 0)
    messages
    (loop [message (first messages)
           messages messages
           idx 0
           res (transient [])]
      (if (= idx 0)
        (conj! res (assoc message :time-string
                          (-> (moment (:created_at message))
                              (.format "ll"))))
        (let [prev-message (nth messages (- idx 1))
              prev-message-moment (moment (:created_at prev-message))
              curr-message-moment (moment (:created_at message))]
          (if (not= (-> prev-message-moment
                        (.format "ll"))
                    (-> curr-message-moment
                        (.format "ll")))
            (conj! res (assoc message :time-string (-> curr-message-moment (.format "ll"))))
            (conj! res (assoc message :time-string nil)))))
      (if (or (= (count messages) 0)
              (= idx (- (count messages) 1)))
        (persistent! res)
        (recur (nth messages (+ idx 1))
               messages
               (+ idx 1)
               res)))))

(defn- transform-messages
  "transform message by group message,
  if messages' created time less than one minutes
  then group them together (by add a field :t)"
  [messages]
  (if (<= (count messages) 0) messages
      (loop [message (nth messages 0)
             messages messages
             idx 0
             res (transient [])]
        (if (= 0 (count messages))
          messages
          (let [prev-message (if (= idx 0) nil (-> (nth messages (- idx 1))))
                cur-message-moment  (moment (:updated_at message))
                prev-message-moment (moment (if (= idx 0) nil (-> (nth messages (- idx 1))
                                                                  :updated_at)))
                message' (if (and
                              (= (:name prev-message) (:name message))
                              (< (-> cur-message-moment
                                     (.diff prev-message-moment "minutes")) 1))
                           (assoc message :t "message")
                           (assoc message :t "head"))]
            (as-> (conj! res message') r
              (if (= (- (count messages) 1) idx)
                (persistent! r)
                (recur (nth messages (+ idx 1))
                       messages
                       (+ idx 1)
                       r))))))))

(defn room
  []
  (let [sub (re-frame/subscribe [::db/subscribe [:current-channel :current-route]])]
    (fn []
      (let [channel-uuid (-> @sub :current-route :path-params :uuid)
            channel-type (-> @sub :current-route :path-params :channel-type)
            current-channel (-> @sub :current-channel)
            messages (-> @sub :current-channel :messages
                         transform-messages
                         add-timeline-string-to-messages)]
        (when-not (= channel-uuid (:channel_uuid current-channel))
          (re-frame/dispatch [::direct-event/current-channel (keyword channel-type) channel-uuid]))
        [:main.flex.flex-col.flex-1
         [room-header current-channel]
         [message-contents
          (when (= (:type current-channel) "direct")
            [message-intro current-channel])
          (for [message messages]
            ^{:key (:uuid message)} [:<>
                                     (when (some? (:time-string message))
                                       [message-date-line (:time-string message)])
                                     [message-card {:avatar (:name message)
                                                    :t (:t message)
                                                    :name (:name message)
                                                    :username (:username message)
                                                    :time (:created_at message)
                                                    :message (:msg message)}]])]
         [message-box]]))))

