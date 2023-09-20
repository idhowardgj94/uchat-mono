(ns com.howard.uchat.db 
  (:require
    [re-frame.core :as re-farme]))

(defonce default-db
  {:name "re-frame"
   :current-route nil
   :user-context-status nil
   :direct-subscriptions nil
   :channel-subscriptions nil
   :user nil
   :teams nil
   :current-team nil
   :current-channel nil
   :auth? nil
   :token nil
   :message-box {}
   :register-validate []
   :register-request {}
   :login-validate []
   :login-error-message nil
   :login-request {}})

(re-farme/reg-sub ::subscribe
                  (fn [db [_ query-v]]
                    (select-keys db query-v)))
