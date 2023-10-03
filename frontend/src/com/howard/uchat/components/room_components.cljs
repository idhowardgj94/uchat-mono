(ns com.howard.uchat.components.room-components
  (:require
   [com.howard.uchat.components.utilities :refer [get-childern ]]
   ["react-icons/ci" :refer [CiUser]]
   ["react-icons/ai" :refer [AiOutlineStar AiOutlinePhone
                             AiOutlineInfoCircle AiOutlineMessage
                             AiOutlineUsergroupAdd AiOutlineSearch]]
   ["react-icons/go" :refer [GoHash GoCommentDiscussion]]
   ["react-icons/gr" :refer [GrAttachment]]
   ["react-icons/bs" :refer [BsThreeDotsVertical]]
   ["moment" :as moment]
   ["react-time-ago" :as time-ago]
   ["react-avatar$default" :as Avatar]))


(defn action-button
  [icon]
  [:button.hover:bg-gray-200 {:className "p-0.5"}
   [:> icon {:size 18}]])
;; TODO: should use name instead of username 
(defn room-header [current-channel]
  (let [{name :other_name} current-channel]
    [:section.basis-16.flex.w-full.items-center.border-b.border-gray-200
     [:div.flex.flex-1.px-6.items-center
      [:> Avatar {:name name :size 36}]
      [:div.flex-1.flex-col.px-2.mx-2
       [:div.flex-1
        [:> GoHash {:style {:display "inline"
                            :margin  "0 4px"}}]
        [:span.font-bold.mr-2.text-lg name]
        [:> AiOutlineStar {:className "inline" :size 20}]]
       (comment "TODO: need to be a dynamic component for comment")
       #_[:div.flex-1.text-sm "TODO: How to ask for help"]]
      [:div
       [:ul.flex {:style {:gap "1em"}}
        [:li [action-button AiOutlinePhone]]
        [:li [action-button AiOutlineInfoCircle]]
        [:li [action-button AiOutlineMessage]]
        [:li [action-button GoCommentDiscussion]]
        [:li [action-button AiOutlineUsergroupAdd]]
        [:li [action-button AiOutlineSearch]]
        [:li [action-button GrAttachment]]
        [:li [action-button BsThreeDotsVertical]]]]]]))


;;
(defn message-intro [current-channel]
  [:div.mt-3.flex.justify-center.flex-col.items-center
   [:div
    [:> Avatar {:name (:other_name current-channel) :className "rounded" :size 49}]]
   [:div.my-4
    [:p.text-lg.font-bold "You have joined a new direct message with"]]
   [:div.flex-1
    [:button.bg-gray-300 {:className "rounded p-0.5 hover:bg-gray-100"}
     [:span.inline-block.mr-1
      [:> CiUser]]
     [:span.font-semibold.text-sm (:other_name current-channel)]]]])

(defn message-date-line
  "given a date string, show a date string seprate line."
  [date-string]
  [:div.flex.items-center.p-1
   [:div.flex-1.separate-line.flex.flex-col.p-1 ""]
   [:div.text-xs.font-semibold date-string]
   [:div.flex-1.separate-line.flex.flex-col.p-1 ""]])

(defn message-contents
  [opt & children]
  (let [children' (get-childern opt children)]
    [:div.flex.flex-col.flex-1.overflow-auto.h-full {:name "wraper"}
     [:div.pt-5
      children']]))

(defn message-card
  "This is message component
   it separte two type: first and second.
   second contain only message
   and first contain Avatar, username and time.
   props:
      t: head or message, default: head.
      avatar: string or component"
  [props]
  (let [{:keys [t avatar name username time message] :or {t "head"}} props
        over-a-day? (let [time-moment (moment (js/Date. time))
                          cur-moment (moment (js/Date.))]
                      (> (-> cur-moment (.diff time-moment "hours")) 24))]

    (case t
      "head" [:div.px-5.flex.hover:bg-gray-200
              [:> Avatar {:name avatar :className "rounded pt-1.5" :size 36}]
              [:div.flex.flex-col.px-2
               [:div.flex.items-center
                [:div.text-sm.font-semibold.mr-1 name]
                [:span.mr-1.text-xs.text-gray-500 (str "@" username)]
                [:span.mr-1.text-xs.text-gray-500 (if over-a-day? (-> (moment time)
                                                                      (.format "YYYY-MM-DD HH:mm:ss")) [:> time-ago {:date time}])]]
               [:div.flex-1
                message]]]
      "message" [:div.px-5.flex.hover:bg-gray-200.items-center {:style {:padding-left "65px"}}
                 [:div.flex-1
                  message]])))

