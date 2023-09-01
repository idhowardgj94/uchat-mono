(ns com.howard.uchat.api
  (:require
   [ajax.core :refer [GET POST]]))

(defonce endpoint "http://localhost:4000")

(defn test-endpoint
  "this is test for uchat backend endpoint
  handler: callback fn"
  [handler]
  (js/console.log "666")
  (GET (str endpoint "/") {:handler handler
                           :error-handler handler
                           :response-format :json
                           :keywords? true}))

(defn register
  "register user to uchat
  params:
  map: :user :email :password :username"
  ([payload handler]
   (register payload handler #(js/console.error (clj->js %))))
  ([payload handler error-handler]
   (POST (str endpoint "/api/v1/register") {:params payload
                                            :format :json
                                            :keywords? true
                                            :error-handler error-handler
                                            :handler handler})))

(comment
  (register {:name "client0355"
             :password "123445"
             :email "client0535@gmail.com"
             :username "clie3nt52034"} #(js/console.log (clj->js %)))
  )
