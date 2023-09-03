(ns com.howard.uchat.views.register
  (:require
   [reitit.frontend.easy :as rfe]
   [re-frame.core :as re-farme]
   [com.howard.uchat.use-cases.core-cases :as event]
   [com.howard.uchat.util :refer [get-form-map contains-in-vector?]]
   [com.howard.uchat.components.basic :refer [form-group label input button]]
   [com.howard.uchat.components.layout :refer [guest-layout]]
   [cljs.core :as c]))

(re-farme/reg-sub
 ::register-validate
 (fn [db _]
   (:register-validate db)))


(defn register-form []
  (let [errors @(re-farme/subscribe [::register-validate])]
    [:div.rounded.overflow-hidden.shadow-lg.bg-white.p-2
     [:h2.text-2xl.font-bold.m-4 "Create an account"]
     [:form.mx-4 {:on-submit #(do (.preventDefault %)
                                  (let [form (-> %
                                                 (.-target))
                                        form-map (get-form-map form)]
                                    (re-farme/dispatch [::event/register form-map])))}
      [form-group
       [label {:for "name"
               :className " font-bold"} "Name*"]
       [input {:name "name"
               :id "name"
               :on-change #(re-farme/dispatch [::event/clear-register-validate])
               :error? (contains-in-vector? errors :name)
               :error-msg  "This field is required"}]]
      [form-group
       [label {:for "email"
               :className " font-bold"} "Email*"]
       [input {:name "email"
               :id "email"
               :on-change #(re-farme/dispatch [::event/clear-register-validate])
               :error? (contains-in-vector? errors :email)
               :error-msg  "This field is required"}]]
      [form-group
       [label {:for "username"
               :className " font-bold"} "username*"]
       [input {:name "username"
               :id "username"
               :on-change #(re-farme/dispatch [::event/clear-register-validate])
               :error? (contains-in-vector? errors :username)
               :error-msg  "This field is required"}]]
      [form-group
       [label {:for "password"
               :className " font-bold"} "Password*"]
       [input {:name "password"
               :id "password"
               :on-change #(re-farme/dispatch [::event/clear-register-validate])
               :error? (contains-in-vector? errors :password)
               :error-msg  "This field is required"}]]
      [form-group
       [label {:for "confirm-password"
               :className " font-bold"} "Confirm password*"]
       [input {:name "confirm-password"
               :id "confirm-password"
               :on-change #(re-farme/dispatch [::event/clear-register-validate])
               :error? (contains-in-vector? errors :confirm-password)
               :error-msg  "This field should be same as password"}]]
      [:div.flex.items-center.pb-4
       [button {:word "register"}]
       [:div.inline-block.text-right.flex-1.font-light.text-sm "Already has account? "
        [:a.text-blue-600.font-light.text-sm {:href (rfe/href :routes/login)} "Back to login"]]]]]))

(defn register []
  [guest-layout
   [register-form]])
