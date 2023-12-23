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

