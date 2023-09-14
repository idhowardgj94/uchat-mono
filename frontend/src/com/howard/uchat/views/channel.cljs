(ns com.howard.uchat.views.channel
  (:require
   [com.howard.uchat.views.rooms :refer [room]]
   [re-frame.core :as re-frame]
   [com.howard.uchat.use-cases.core-cases :as event]
   [com.howard.uchat.db :as db]))

(defn create-direct
  []
  (let [{:keys [current-route]} @(re-frame/subscribe [::db/subscribe [:current-route]])
        other-user (-> current-route
                       :path-params
                       :other-username)]
    (re-frame/dispatch [::event/create-direct other-user])
    (fn []
      [:div "handling...."])))
(defn guard
  []
  (let [auth (re-frame/subscribe [::db/subscribe [:auth?]])
        auth? (:auth? @auth)]
    (if (= auth? true)
      true
      (do
        (println "inside else")
        (re-frame/dispatch [:routes/navigate :routes/login])
        false))))
(defn channel
  "This is a channel layout."
  []
 (guard)
  [room])

(defn home
  []
  (guard)
  [:div.p-2 "welcome to uchat."])
