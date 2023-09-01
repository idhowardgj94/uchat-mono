(ns com.howard.uchat.routes
  (:require
   [re-frame.core :as rf]
   [reitit.frontend :as rtf]
   [reitit.frontend.easy :as rtfe]
   [reitit.coercion.schema :as rsc]
   [com.howard.uchat.views.home :as home]
   [com.howard.uchat.views.compo :as compo]
   [com.howard.uchat.views.login :refer [login]]
   [com.howard.uchat.views.register :refer [register]]
   [com.howard.uchat.views.channel :as channel]
   [com.howard.uchat.views.rooms :refer [room]]
   [tools.reframetools :refer [sdb gdb]]))

;;https://clojure.org/guides/weird_characters#__code_code_var_quote
(def routes
    (rtf/router
     [["/login" {:name :routes/login
                 :view #'login}]
      ["/register" {:name :routes/register
                    :view #'register}]
       ["/"
        {:name :routes/#frontpage
         :view #'home/main}]
       ["/component"
        {:name :routes/#component
         :view #'compo/main}]
      ["/room"
       {:name :routes/room-container
        :view #'room}]
      ["/layout"
       {:name :routes/layout
        :view #'channel/channel}]]
      {:data {:coercion rsc/coercion}}))

(defn on-navigate [new-match]
  (when new-match
    (rf/dispatch [:routes/navigated new-match])))

(defn app-routes []
  (rtfe/start! routes
               on-navigate
               {:use-fragment true}))

(rf/reg-sub
 :routes/current-route
 (gdb [:current-route]))

;;; Events
(rf/reg-event-db
 :routes/navigated
 (sdb [:current-route]))

(rf/reg-event-fx
 :routes/navigate
 (fn [_cofx [_ & route]]
   {:routes/navigate! route}))


