(ns tools.viewtools
  (:require [reitit.frontend.easy :as rtfe]))


(defn item [e]
  (cond
     (fn? e) [e]
     (vector? e) e
     (string? e) [:h2 e]))

(defn panel [name component]
  [:div

   [item name]
   [item component]])

;; navigation tools
(defn sep []
  [:span " | "])

(defn nav-item [i]
  (if (= :sep i)
    [sep]
    [:a.text-blue-700
     {:href (rtfe/href (second i) (if (>= (count i) 3) (nth i 2) nil))} (first i)]))

(defn navigation [routes]
  (let [coll (->> routes (interpose :sep) (map-indexed vector))]
    [:div
     (for [[idx rt]  coll]
       ^{:key (str idx)} [nav-item rt])]))
