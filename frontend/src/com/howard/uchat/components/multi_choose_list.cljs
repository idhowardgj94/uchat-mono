(ns com.howard.uchat.components.multi-choose-list
  (:require 
   [spec-tools.data-spec :as ds]
   [cljs.spec.alpha :as s]
   ))

(def multi-choose-list-opt
  (ds/spec
   {:name ::multi-choose-list-opt
    :spec {:data [{:text string?
                   (ds/opt :on-click) fn?
                   :id (ds/or {:string string? :number number?})
                   :avatar vector?}]}}))

(defn multi-choose-list
  "multi select list, used when user need to select multiple items, like users.
  opt:
  on-click (function) - map of keys below
  :event (event) - click event
  :value (string) - value of this checkbox, i.e. title for now
  :checked (bool) - if checked."
  [opt]
  (when-not (s/valid? multi-choose-list-opt opt)
    (js/console.error  (s/explain-str multi-choose-list-opt opt)))
  (let [{:keys [on-click]} opt]
    [:div.block
     (for [{:keys [text id avatar]} (-> opt :data)]
       [:div.flex.cursor-pointer  {:key id
                                   :on-click (fn [e]
                                               (when-let [elem (-> e .-target)]
                                                 (if (or (object? elem)
                                                         (not= (-> elem .-tagName) "INPUT"))
                                                   (let [it (-> elem (.querySelector "input"))]
                                                     (.click it))
                                                   (when (fn? on-click)
                                                     (on-click {:event e
                                                                :value (-> e .-target .-value)
                                                                :checked (-> e .-target .-checked)})))))}
        [:div.inline-flex.border-b.flex-1.items-center
         [:label {:className "relative flex items-center p-3 rounded-full cursor-pointer"
                  :for "checkbox"}
          [:input {:type "checkbox"
                   :name "checkbox"
                   :value text
                   :className "peer relative h-5 w-5 cursor-pointer appearance-none rounded-md border transition-all checked:border-pink-500 checked:bg-pink-500"}]
          [:div {:className "absolute text-white transition-opacity opacity-0 pointer-events-none top-2/4 left-2/4 -translate-y-2/4 -translate-x-2/4 peer-checked:opacity-100"}
           [:svg {:xmlns "http://www.w3.org/2000/svg"
                  :className "h-3.5 w-3.5"
                  :viewBox "0 0 20 20"
                  :fill "currentColor"
                  :stroke "currentColor"
                  :strokeWidth "1"}
            [:path {:fillRule "evenodd"
                    :d "M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                    :clipRule "evenodd"}]]]]
         avatar
         [:div.ml-2.text-xl text]]])]))


