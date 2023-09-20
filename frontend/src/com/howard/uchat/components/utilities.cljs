(ns com.howard.uchat.components.utilities)


(defn get-childern
  "This function is a utility function to get reagent childdren
  there are two cases: if the first opt is a vector, then it means
  that this components has no options field, then return all of childern.
  else means that first params is opt, just return rest params"
  [opt & children]
  (if (vector? opt) (->> (cons opt children)
                        (map #(if (list? %) (first %) %))) children))

;; TODO: rename to is-opts 
(defn get-opts
  "give opt and to see if it's a vector
  if it's not vector, then return nil
  TODO: if no use, then delete later."
  [opt]
  (if (map? opt) opt nil))

(defn >children
  "TODO: type check"
  [children]
  (let [children' (->> children
                       ;; TODO:  children may be like ([:div] ([:span])),
                       ;; second one means that use & to get reamain argument,
                       ;; which type is indexedseq,
                       ;; so get the first element in it.
                       ;; here I give a asumption, childern must contain only one
                       ;; hiccup vector, and it may cause bug and need refactor.
                       (map #(if (= IndexedSeq (type %)) (first %) %)))]
    [:<>
     (for [[idx child] (map-indexed vector children')]
       ^{:key idx} [:<> (if (list? child) (first child) child)])]))
(defn popup
  "popup component
   opt: style, open
  "
  [opt & children]
  (let [style-in (:style opt)
        class-name (:className opt)
        open? (cond
                (boolean? (:open opt)) (:open opt)
                :else (if (nil? (:open opt)) true @(:open opt)))
        children'  (if (vector? opt) (cons opt children) children)]
    (when open?
      [:div.block.absolute.w-64.bg-white.overflow-auto
       {:style (assoc style-in :top "calc(100% + 4px)")
        :className (str class-name " text-black shadow-lg border border-gray-200")}
       [>children children']])))


(defn verticle-line
  "a vertical line used to seperate items."
  []
  [:div.bg-gray-100.block {:style {:height "1px"}}])
