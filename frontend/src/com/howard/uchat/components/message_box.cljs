(ns com.howard.uchat.components.message-box
  (:require [reagent.ratom :as r]
            [re-frame.core :as re-frame]
            [day8.re-frame.tracing :refer-macros [fn-traced]]  
            [com.howard.uchat.use-cases.core-cases :as core-event]
            [com.howard.uchat.use-cases.direct :as event]
            [com.howard.uchat.db :as db]
            ["react-icons/ci" :refer [CiUser]]
            ["react-icons/ai" :refer [AiOutlineStar AiOutlinePhone
                                      AiOutlineInfoCircle AiOutlineMessage
                                      AiOutlineUsergroupAdd AiOutlineSearch]]
            ["react-icons/fi" :refer [FiSend]]
            ["react-icons/go" :refer [GoHash GoCommentDiscussion]]
            ["react-icons/gr" :refer [GrAttachment]]
            ["react-icons/bs" :refer [BsThreeDotsVertical]]
            ["react-avatar$default" :as Avatar]
            ))

(re-frame/reg-sub
 ::get-messagebox-by-channel-id
 (fn-traced
  [db [_ channel-id]]
  (-> db
      :message-box
      (get (keyword channel-id)))))

(defn message-box
  []
  (let [sub (-> (re-frame/subscribe [::db/subscribe [:current-channel]]))]
    (fn []
      (let [channel-uuid (-> @sub :current-channel :channel_uuid)
            message (re-frame/subscribe [::get-messagebox-by-channel-id channel-uuid])]
        [:div.flex.basis-5.shrink-0
         [:div.w-ull.my-4.px-3.flex-1.items-center.justify-center.flex.flex-1.flex-col
          [:div.w-full.pt-2.pb.1.px-5
           [:div.border-2.border-gray-200.focus:ring-blue-800.focus:ring-1
            {:className
             "focus-within:ring-blue-400 focus-within:ring-2"}
            [:textarea.w-full.appearance-none
             {:className "focus:outline-none"
              :value (or @message "")
              :on-change (fn [e]
                           (let [value (-> e .-target .-value)]
                             (re-frame/dispatch [::core-event/assoc-in-db [:message-box (keyword channel-uuid)] (str value)])))
              :style {:resize "none"
                      :-webkit-appearance "none"}}]
            [:div.flex
             [:div.flex-1.bg-gray-200
              ""]
             [:button.mr-0.ml-auto.p-2.rounded.bg-blue-500.hover:bg-blue-700
              {:on-click (fn [e]
                           (re-frame/dispatch [::event/send-message @message]))}
              [:> FiSend {:style {:color "white"}}]]]]]]]))))
