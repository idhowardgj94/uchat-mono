(ns com.howard.uchat.backend.tools.macro)


(defmacro export-fn
  "export a function by given name and a function ref"
  [sym f]
  (let [{:keys [doc arglists]} (if (coll? f)
                                 (meta (resolve (symbol (eval f))))
                                 (meta (resolve f)))]
    ;; note: arglists type will like ([a])
    ;; so need to change to quote @arglists (quote ([a]))
    `(def
       ~(with-meta sym {:doc doc
                        :arglists `(quote ~arglists)}) ~f)))

(defmacro dbfn
  {:clj-kondo/lint-as 'clojure.core/defn}
  [name & args]
  (let [doc (if (string? (first args)) (first args) nil)
        params (if (nil? doc) (first args) (second args))
        second-params (if (<= (count params) 1) [] (into [] (drop 1 params)))
        body (if (nil? doc) (drop 1 args) (drop 2 args))]
    `(defn ~name
         ~doc
         (~params
         ~@body)
         (~second-params
          (apply ~name (get-pool) ~second-params)))))
