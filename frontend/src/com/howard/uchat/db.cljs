(ns com.howard.uchat.db 
  (:require
    [re-frame.core :as re-farme]))

(defonce default-db
  {:name "re-frame"
   :current-route nil
   :current-sub-route nil
   :auth? nil
   :token nil
   :register-validate []
   :register-request {}
   :login-validate []
   :login-error-message nil
   :login-request {}})

(re-farme/reg-sub ::subscribe
                  (fn [db [_ query-v]]
                    (select-keys db query-v)))
