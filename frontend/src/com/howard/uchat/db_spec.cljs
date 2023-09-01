(ns com.howard.uchat.db-spec
  (:require
   [spec-tools.data-spec :as ds]
   [cljs.spec.alpha :as s]))


;; TODO 
;(s/def ::user string?)
;(s/def ::email string?)
;(s/def ::auth  (s/nilable (s/keys :req-un [::user ::email])))

;(s/def ::status #{:success :fail :validate-error})

;(s/def ::response (s/nilable map?))

;(s/def ::register-request (s/nilable (s/keys :req-un [::status ::response])))

(def post-login-spec
  (ds/spec {:name ::post-login-spec
            :spec {:username string?
                   :password string?}}))

(defn- not-empty-string?
  [s]
  (and (string? s) (not= "" (str s))))
(def post-user-spec
  (ds/spec {:name ::post-user-spec
            :spec {:username not-empty-string?
                   :password not-empty-string?
                   :name not-empty-string?
                   :confirm-password not-empty-string?
                   :email not-empty-string?}}))
