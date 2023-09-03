(ns com.howard.uchat.api
  #_{:clj-kondo/ignore [:unresolved-var]}
  (:require
   [ajax.core :refer [GET POST]]))

(defonce endpoint "http://localhost:4000")

(def json-request
  "This is a default json request setting for uchat."
  {:format :json
   :keywords? true
   :response-format :json})

(defn register
  "register user to uchat
  params:
  map: :user :email :password :username"
  ([payload handler]
   (register payload handler #(js/console.error (clj->js %))))
  ([payload handler error-handler]
   #_{:clj-kondo/ignore [:unresolved-var]}
   (POST (str endpoint "/api/v1/register") (merge json-request
                                                  {:params payload
                                                   :error-handler error-handler
                                                   :handler handler}))))

(defn login
  "login user to uchat
  params:
  map :username :password"
  ([payload handler]
   (login payload handler #(js/console.error (clj->js %))))
  ([payload handler error-handler]
   #_{:clj-kondo/ignore [:unresolved-var]}
   (POST (str endpoint "/api/v1/login") (merge json-request
                                               {:params payload
                                                :error-handler error-handler
                                                :handler handler}))))
(comment
  (register {:name "client0355"
             :password "123445"
             :email "client0535@gmail.com"
             :username "clie3nt52034"} #(println %))
  (login {:username "idhowardgj94"
          :password "Eva831219"}
         #(println %))
  )
