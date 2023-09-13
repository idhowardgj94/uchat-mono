(ns com.howard.uchat.api
  #_{:clj-kondo/ignore [:unresolved-var]}
  (:require
   [com.howard.uchat.db :as db]
   [ajax.core :refer [GET POST] :as ajax]
   [re-frame.core :as re-farme]))


(defonce endpoint "http://localhost:4000")

(def json-request
  "This is a default json request setting for uchat."
  {:format :json
   :keywords? true
   :response-format (ajax/json-response-format {:keywords? true})})

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

(defn get-subscription
  ([payload handler]
   (get-subscription payload handler #(js/console.warn %)))
  ([payload handler error-handler]
   (let [sub (re-farme/subscribe [::db/subscribe [:token]])
         token (:token @sub)]
     (GET (str endpoint "/api/v1/subscriptions")
       (merge json-request
              {:params payload
               :headers {"authorization" (str "token " token)}
               :handler handler
               :error-handler error-handler})))))

(comment
  (get-subscription {:type "direct"
                     :team_uuid "bd9a04af-899d-4d61-a169-8dba5dca99d8"} #(print %))
  )

(comment
  (register {:name "client0355"
             :password "123445"
             :email "client0535@gmail.com"
             :username "clie3nt52034"} #(println %))
  (login {:username "idhowardgj94"
          :password "Eva831219"}
         #(println %)))
