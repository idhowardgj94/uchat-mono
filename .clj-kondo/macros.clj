(ns macros)

(defmacro dbfn
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
          (apply ~name (com.howard.uchat.backend.database.interface/get-pool) ~second-params)))))
