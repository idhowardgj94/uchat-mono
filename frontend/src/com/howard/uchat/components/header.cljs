(ns com.howard.uchat.components.header
  (:require
   ["react-avatar$default" :as Avatar]
   ["react-icons/si" :refer [SiGooglemessages SiIconify]]
   ["react-icons/sl" :refer [SlLogout]]
   [com.howard.uchat.components.utilities :refer
    [popup verticle-line]]
   [reagent.core :as r]))

(defn popup-status-bar
  []
  [:div
   [:button.hover:bg-gray-200.w-full.my-2
    [:div.px-4.w-full.flex.items-center
     [:> Avatar {:name "Howard" :className "rounded-full" :size 36}]
     [:div.p-3 "@lhchangq"]]]
   [verticle-line]
   [:button.hover:bg-gray-200.w-full.my-2
    [:div.px-4.w-full.flex.items-center
     [:> SiIconify {:size "14px" :color "gray"}]
     [:div.p-2.text-gray-400.text-sm "Set a Custom Status"]]]
   [verticle-line]
   [:button.hover:bg-gray-200.w-full.my-2
    [:div.px-4.w-full.flex.items-center
     [:> SlLogout {:size "14px"}]
     [:div.p-2 "Logout"]]]])

(defn header []
  (let [popup-open? (r/atom false)]
    [:header.basis-10.flex.items-center.px-3.shrink-0
     {:style {:background-color "#14213e"
              :color "rgba(255, 255, 255, 0.64)"}}
     [:> SiGooglemessages {:size 24}]
     [:h1.font-lg.ml-2 "Channel"]
     [:div.relative.mr-0.ml-auto
      [:button {:on-click #(swap! popup-open? not)}
       [:div.rounded-full.overflow-hidden
        [:> Avatar {:name "Howard" :size 30}]]
       (comment "status light")
       [:span.rounded-full.bg-yellow-200.z-50.absolute.inline-block
        {:style {:min-width 10
                 :min-height 10
                 :right "0px"
                 :bottom "4px"}}]]
      [popup {:style {:right "1px"}
              :open popup-open?}
       [popup-status-bar]]]]))

