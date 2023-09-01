(ns com.howard.uchat.views.login
  (:require
   [com.howard.uchat.components.layout :refer [guest-layout]]))

(defn login-form []
  [:div.rounded.overflow-hidden.shadow-lg.bg-white.p-2
   [:h2.text-2xl.font-bold.m-4 "Login"]
   [:form.mx-4
    [:div.mb-6
     [:label.block.text-sm.font-light.mb-1.leading-6 {:for "username"} "Email or username"]
     [:div.flex.rounded-md.shadow-sm.ring-1.ring-inset.ring-gray-300.appearance-none
      {:className "focus-within:ring-2 focus-within:ring-inset focus-within:ring-indigo-600"}
      [:input.flex-1.block.border-0.bg-transparent.pl-1.pl-4
       {:className "focus:ring-0 focus:outline-none py-1.5"
        :name "username"
        :id "username"}]]
     [:span.text-red-500 "This field is required"]]
    [:div
     [:label.block.text-sm.font-light.mb-1.leading-6 {:for "password"} "Password"]
     [:div.flex.rounded-md.shadow-sm.ring-1.ring-inset.ring-gray-300.appearance-none.ring-red-500
      {:className "focus-within:ring-2 focus-within:ring-inset focus-within:ring-indigo-600"}
      [:input.flex-1.block.border-0.bg-transparent.pl-1.pl-4
       {:className "focus:ring-0 focus:outline-none py-1.5"
        :name "password"
        :type "password"
        :id "password"}]]
     [:span.text-red-500 "This field is required"]]
    [:div.text-right
     [:a.text-right.font-light.text-blue-600.hover:underline.text-sm {:href "#"} "Forgot your password?"]]
    [:div.flex.items-center.py-4
     [:button.rounded-none.bg-blue-600.hover:bg-blue-700.px-4.text-white
      {:className "py-1.5"}
      "Login"]
     [:div.inline-block.text-right.flex-1.font-light.text-sm "new here? "
      [:a.text-blue-600.font-light.text-sm {:href "#"} "Create an account"]]]]])

(defn login []
  [guest-layout
   [login-form]])
