(ns com.howard.uchat.db)

(defonce default-db
  {:name "re-frame"
   :auth? nil
   :token nil
   :register-validate []
   :register-request {}
   :login-validate []
   :login-request {}})

