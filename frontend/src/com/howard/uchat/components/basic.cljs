(ns com.howard.uchat.components.basic
  "collect some basic component used in this project.
  for example, button, input form-group, etc.."
  (:require
   [com.howard.uchat.components.utilities :refer [get-opts get-childern ]]))

(defn label
  "label for form
  opt: map, require: for
  name string"
  [opts name]
  (let [opts (get-opts opts)]
    [:label.block.text-sm.mb-1.leading-6 opts name]))



(defn input
  "input for uchat. contain default style.
  opts: name, id
  :on-click - input's onClick handler
  :error-msg - string
  :id - input's id
  :name - input's name
  :on-change - input's onChange
  :error? - atom of boolean or boolean"
  [opts]
  (let [error-msg (:error-msg opts)
        error-field (:error? opts) 
        error (if (instance? cljs.core.Atom error-field) @error-field error-field)
        ; if not set error?, then default true to open msg
        error? (if (nil? error) true error)]
    [:<>
     [:div.flex.rounded-md.shadow-sm.ring-1.ring-inset.ring-gray-300.appearance-none
      {:className "focus-within:ring-2 focus-within:ring-inset focus-within:ring-indigo-600"}
      [:input.flex-1.block.border-0.bg-transparent.pl-1.pl-4
       (merge {:className "focus:ring-0 focus:outline-none py-1.5"} (select-keys  opts [:name :id :on-change :on-click]))]]
     (and error?
          [:span.text-red-500 (or error-msg "This field is required")])]))

(defn button
  "button used in uchat
  TODO: need to be more generic
  opts: on-click & word is important of all"
  [opts]
  (let [word (:word opts)
        opts' (dissoc opts :word)]
    [:button.rounded-none.bg-blue-600.hover:bg-blue-700.px-4.text-white
     (merge {:className "py-1.5"} opts')
     word]))

(defn form-group
  "from group used in uchat."
  [opts & children]
  (let [opts' (get-opts opts)
        children' (get-childern opts children)]
    [:div.mb-6 opts'
     children']))

