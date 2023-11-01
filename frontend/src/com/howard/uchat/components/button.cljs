(ns com.howard.uchat.components.button)


(defn button
  "a button"
  [opt]
  (let [{:keys [text color]} opt]
    [:button.rounded-none.bg-blue-600.hover:bg-blue-700.px-4.text-white
     {:className (str "py-1.5"
                      (case color
                        "yellow" " bg-yellow-600 hover:bg-yellow-700"
                        " bg-blue-600 hover:bg-blue-700"))}
     text]))
