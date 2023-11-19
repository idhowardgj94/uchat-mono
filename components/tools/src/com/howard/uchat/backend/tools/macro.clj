(ns com.howard.uchat.backend.tools.macro)


(defmacro export-fn
  "export a function by given name and a function ref"
  [sym f]
  (let [{:keys [doc arglists]} (meta (resolve f))]
    `(def  
       ~(with-meta sym {:doc doc
                        :arglists `'~arglists
                        }) ~f)))
