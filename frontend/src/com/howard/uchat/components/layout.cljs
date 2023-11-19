(ns com.howard.uchat.components.layout
  "This namespace contains all layout be used in this project"
  (:require
   [com.howard.uchat.components.header :refer [header]]
   [com.howard.uchat.components.leftbar :refer [leftbar]]
   [com.howard.uchat.components.utilities :refer
    [get-childern get-opts]]
   [com.howard.uchat.styles :as s]
   [com.howard.uchat.use-cases.core-cases :as event]
   [re-frame.core :as re-frame]))



(re-frame/reg-sub ::auth?
                  (fn [db _]
                    (:auth? db)))
                                        ;
(defn main-layout
  "this is main layout for uchat."
  []
  (re-frame/dispatch [::event/prepare-user-context])
  (fn []
    (let [active-route (re-frame/subscribe [:routes/current-route])
          view (:view (:data @active-route))]
      [:div.h-screen.flex.flex-col
       [header]
       [:div.flex.flex-1.overflow-auto
        [leftbar]
        [view]]])))



(defn guest-layout
  "guest layout is used when user is not login yet."
  [opts & children]
  (let [opt (get-opts opts)
        children' (get-childern opts children)
        auth? (re-frame/subscribe [::auth?])]
    (when (= true @auth?)
      (re-frame/dispatch [:routes/navigate :routes/channels-home]))
    (fn []
      [:div.w-screen.h-screen.overflow-auto (assoc-in opt [:style :background-image] s/login-background)
       [:div.container.mx-auto.flex.max-w-6xl.overflow-auto.h-full.items-center
        [:section.flex-1
         [:h1.text-6xl  "Welcome to
                                      UChat workspace"]]
        [:section.flex-1.items-center.justify-center
         children']]])))
  
