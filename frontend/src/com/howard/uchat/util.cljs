(ns com.howard.uchat.util
  "This namespace use to store some util function that may be used in
  whole project.")


(defn get-form-map
  "Get the form map from submit form
  recived a form dom,
  and return a map of formdata"
  [form]
  (let [form-data (js/FormData. form)]
    (->> (.from js/Array (.entries form-data))
         (js->clj)
         (map (fn [it] {(keyword (first it)) (second it)}))
         (reduce merge))))
