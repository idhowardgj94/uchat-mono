(ns com.howard.uchat.views.home
  (:require
    [re-frame.core :as re-frame]
    [tools.viewtools :as vt]
    [com.howard.uchat.components.layout :refer [main-layout]]
    [com.howard.uchat.db :as db]
    ))


(defn main []
  [:div
   {:class ["h-[220px]" "w-[300px]" :bg-blue-50 :m-4 :p-2 "rounded-[5px]"]}
   [:h2.text-4xl "home"]
   [:p "nothing to see here"]])

(def toolbar-items
  "this is a debug purpose components"
  [["#" :routes/#frontpage]
   ["component" :routes/#component]
   ["login" :routes/login]
   ["register" :routes/register]
   ["room" :routes/room-container]
   ["channels" :routes/channels-home]])


(defn main-panel []
  (let [active-route (re-frame/subscribe [:routes/current-route])
        auth (re-frame/subscribe [::db/subscribe [:auth?]])
        view (:view (:data @active-route))]
    [:<>
     [vt/navigation toolbar-items]
     (if (= (:auth? @auth) true)
       [main-layout]
       [:div
        (when (some? view)
          [:<> [view]])])]))
