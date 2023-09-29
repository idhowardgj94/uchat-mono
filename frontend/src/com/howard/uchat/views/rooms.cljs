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
(defn- transform-messages
  "transform message by group message,
  if messages' created time less than one minutes
  then group them together (by add a field :t)"
  [messages]
  (loop [message (nth messages 0)
         messages messages
         idx 0
         res (transient [])]
    (if (= 0 (count messages))
      messages
      (let [cur-message-moment  (moment (:updated_at message))
            prev-message-moment (moment (if (= idx 0) nil (-> (nth messages (- idx 1))
                                                              :updated_at)))
            message' (if (< (-> cur-message-moment
                                (.diff prev-message-moment "minutes")) 1)
                       (assoc message :t "message")
                       (assoc message :t "head"))]
        (conj! res message')
        (if (= (- (count messages) 1) idx)
          (persistent! res)
          (recur (nth messages (+ idx 1))
                 messages
                 (+ idx 1)
                 res))))))

(defn room
  []
  (let [sub (re-frame/subscribe [::db/subscribe [:current-channel :current-route]])]
    (fn []
      (let [channel-uuid (-> @sub :current-route :path-params :uuid)
            channel-type (-> @sub :current-route :path-params :channel-type)
            current-channel (-> @sub :current-channel)
            messages (-> @sub :current-channel :messages
                         transform-messages)]
        (when-not (= channel-uuid (:channel_uuid current-channel))
          (re-frame/dispatch [::direct-event/current-channel (keyword channel-type) channel-uuid]))
        [:main.flex.flex-col.flex-1
         [room-header current-channel]
         [message-contents
          [message-intro current-channel]
          ;; TODO: message date 
          [message-date-line]
          (for [message messages]
            ^{:key (:uuid message)} [:<> [message-card {:avatar (:name message)
                                                        :t (:t message)
                                                        :name (:name message)
                                                        :username (:username message)
                                                        :time (:created_at message)
                                                        :message (:msg message)}]])]
         [message-box]]))))


#_(
   [message-card {:avatar "Howard"
                         :name "Howardtest"
                         :username "HowardTest"
                         :time "2:29 pm"
                         :message "hello, world"}]
          [message-card {:t "message"
                         :message "Something change me inside, that is emacs is a best IDE I've ever use, so I'd like to share this thing with you."}]
          [message-card {:t "message"
                         :message "Generating random paragraphs can be an excellent way for writers to get their creative flow going at the beginning of the day. The writer has no idea what topic the random paragraph will be about when it appears. This forces the writer to use creativity to complete one of three common writing challenges. The writer can use the paragraph as the first one of a short story and build upon it. A second option is to use the random paragraph somewhere in a short story they create. The third option is to have the random paragraph be the ending paragraph in a short story. No matter which of these challenges is undertaken, the writer is forced to use creativity to incorporate the paragraph into their writing. "}]
          [message-card {:avatar "Eva"
                         :name "Eva"
                         :username "EvaTest"
                         :time "2:29 pm"
                         :message " A random paragraph can also be an excellent way for a writer to tackle writers' block. Writing block can often happen due to being stuck with a current project that the writer is trying to complete. By inserting a completely random paragraph from which to begin, it can take down some of the issues that may have been causing the writers' block in the first place. "}]
          [message-card {:t "message"
                         :message "he howard"}]
          [message-card {:t "message"
                         :message "Generating random paragraphs can be an excellent way for writers to get their creative flow going at the beginning of the day. The writer has no idea what topic the random paragraph will be about when it appears. This forces the writer to use creativity to complete one of three common writing challenges. The writer can use the paragraph as the first one of a short story and build upon it. A second option is to use the random paragraph somewhere in a short story they create. The third option is to have the random paragraph be the ending paragraph in a short story. No matter which of these challenges is undertaken, the writer is forced to use creativity to incorporate the paragraph into their writing. "}]
          [message-card {:t "message"
                         :message "Generating random paragraphs can be an excellent way for writers to get their creative flow going at the beginning of the day. The writer has no idea what topic the random paragraph will be about when it appears. This forces the writer to use creativity to complete one of three common writing challenges. The writer can use the paragraph as the first one of a short story and build upon it. A second option is to use the random paragraph somewhere in a short story they create. The third option is to have the random paragraph be the ending paragraph in a short story. No matter which of these challenges is undertaken, the writer is forced to use creativity to incorporate the paragraph into their writing. "}]
          [message-card {:t "message"
                         :message "Generating random paragraphs can be an excellent way for writers to get their creative flow going at the beginning of the day. The writer has no idea what topic the random paragraph will be about when it appears. This forces the writer to use creativity to complete one of three common writing challenges. The writer can use the paragraph as the first one of a short story and build upon it. A second option is to use the random paragraph somewhere in a short story they create. The third option is to have the random paragraph be the ending paragraph in a short story. No matter which of these challenges is undertaken, the writer is forced to use creativity to incorporate the paragraph into their writing. "}]
          [message-card {:t "message"
                         :message "Generating random paragraphs can be an excellent way for writers to get their creative flow going at the beginning of the day. The writer has no idea what topic the random paragraph will be about when it appears. This forces the writer to use creativity to complete one of three common writing challenges. The writer can use the paragraph as the first one of a short story and build upon it. A second option is to use the random paragraph somewhere in a short story they create. The third option is to have the random paragraph be the ending paragraph in a short story. No matter which of these challenges is undertaken, the writer is forced to use creativity to incorporate the paragraph into their writing. "}]
          [message-card {:t "message"
                         :message "Generating random paragraphs can be an excellent way for writers to get their creative flow going at the beginning of the day. The writer has no idea what topic the random paragraph will be about when it appears. This forces the writer to use creativity to complete one of three common writing challenges. The writer can use the paragraph as the first one of a short story and build upon it. A second option is to use the random paragraph somewhere in a short story they create. The third option is to have the random paragraph be the ending paragraph in a short story. No matter which of these challenges is undertaken, the writer is forced to use creativity to incorporate the paragraph into their writing. "}]
          [message-card {:t "message"
                         :message "Generating random paragraphs can be an excellent way for writers to get their creative flow going at the beginning of the day. The writer has no idea what topic the random paragraph will be about when it appears. This forces the writer to use creativity to complete one of three common writing challenges. The writer can use the paragraph as the first one of a short story and build upon it. A second option is to use the random paragraph somewhere in a short story they create. The third option is to have the random paragraph be the ending paragraph in a short story. No matter which of these challenges is undertaken, the writer is forced to use creativity to incorporate the paragraph into their writing. "}]
          [message-card {:t "message"
                         :message "he howard"}]
   ,)
