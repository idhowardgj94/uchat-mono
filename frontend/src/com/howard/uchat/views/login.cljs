(ns com.howard.uchat.views.login
  (:require
   [reitit.frontend.easy :as rfe]
   [re-frame.core :as re-frame]
   [com.howard.uchat.util :refer [get-form-map]]
   [com.howard.uchat.use-cases.core-cases :as event]
   [com.howard.uchat.components.basic :refer [form-group label input button]]
   [com.howard.uchat.components.layout :refer [guest-layout]]
   [re-frame.core :as re-farme]))

(defn login-form []
  [:div.rounded.overflow-hidden.shadow-lg.bg-white.p-2
   [:h2.text-2xl.font-bold.m-4 "Login"]
   [:form.mx-4 {:on-submit (fn [e]
                             (.preventDefault e)
                             (let [form-map (-> e
                                                (.-target)
                                                (get-form-map))]
                               (re-frame/dispatch [::event/login form-map])))}
    [form-group
     [label {:for "username"} "Username"]
     [input  {:name "username"
              :id "username"
              :on-change #(re-farme/dispatch [::event/clear-login-validate])
              :error? false
              :error-msg "This field is required"}]]
    [form-group
     [label {:for "password"} "Password"]
     [input {:name "password"
             :id "password"
             :on-change #(re-frame/dispatch [::event/clear-login-validate])
             :error? false
             :error-msg "This field is required"}]]

    [:div.text-right
     [:a.text-right.font-light.text-blue-600.hover:underline.text-sm {:href "#"} "Forgot your password?"]]
    [:div.flex.items-center.py-4
     [:button.rounded-none.bg-blue-600.hover:bg-blue-700.px-4.text-white
      {:className "py-1.5"}
      "Login"]
     [:div.inline-block.text-right.flex-1.font-light.text-sm "new here? "
      [:a.text-blue-600.font-light.text-sm {:href (rfe/href :routes/register)} "Create an account"]]]]])

(defn login []
  [guest-layout
   [login-form]])
