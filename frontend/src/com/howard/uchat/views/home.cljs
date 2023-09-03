(ns com.howard.uchat.views.home
  (:require
    [re-frame.core :as re-frame]
    [tools.viewtools :as vt]
    ))


(defn main []
  [:div
   {:class ["h-[220px]" "w-[300px]" :bg-blue-50 :m-4 :p-2 "rounded-[5px]"]}
   [:h2.text-4xl "home"]
   [:p "nothing to see here"]])

(def toolbar-items
  "this is a debug purpose components"
  [
   ["#" :routes/#frontpage]
   ["component" :routes/#component]
   ["login" :routes/login]
   ["register" :routes/register]
   ["room" :routes/room-container]
   ["channels" :routes/channels]
   ])

(defn main-panel []
  (let [active-route (re-frame/subscribe [:routes/current-route])
        view (:view (:data @active-route))]
    [:div
     [vt/navigation toolbar-items]
     (when (some? view)
       [:<> [view]])
     ]))
