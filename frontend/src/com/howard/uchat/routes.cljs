(ns com.howard.uchat.routes
  (:require
   [re-frame.core :as rf]
   [reitit.core :as reitit]
   [reitit.frontend :as rtf]
   [reitit.coercion.spec :as rsc]
   [com.howard.uchat.views.home :as home]
   [com.howard.uchat.views.compo :as compo]
   [com.howard.uchat.views.login :refer [login]]
   [com.howard.uchat.views.register :refer [register]]
   [com.howard.uchat.views.channel :as channel]
   [com.howard.uchat.views.rooms :refer [room]]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [reitit.frontend.easy :as rfe]))

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
    ["/channels/home" {:name :routes/channels-home
                       :view #'channel/home}]
    ["/channels/channel/:uuid"
     {:name :routes/channels
      :view #'channel/channel
      :parameters {:path {:uuid string?}}}]
    ["/channels/create-direct/:other-username"
     {:name :routes/create-direct
      :view #'channel/create-direct
      :parameters {:path {:other-username string?}}}]]
   {:data {:coercion rsc/coercion}}))

(defn on-navigate [new-match]
  (when new-match
    (rf/dispatch [:routes/navigated new-match])))

(defn app-routes []
  (rfe/start! routes
               on-navigate
               {:use-fragment true}))

(rf/reg-sub
 :routes/current-route
 (fn-traced [db _]
   (get-in db [:current-route])))

;;; Events
(rf/reg-event-db
 :routes/navigated
 (fn-traced [db [_ v]]
            (assoc-in db [:current-route] v)))  

(rf/reg-event-fx
 :routes/navigate
 (fn [_ [_ route]]
   (condp = (type route)
     cljs.core/Keyword (rfe/push-state route)
     (let [[name items] route]
       (rfe/push-state name items)))))

#_(rf/dispatch [:routes/navigate [:routes/channels {:uuid "oeuoeu"}]])
#_(rf/dispatch [:routes/navigate :routes/channels-home])
