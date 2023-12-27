(ns com.howard.uchat.backend.api-server.spec
  (:require
   [spec-tools.data-spec :as ds]
   [clojure.spec.alpha :as s]))

(def post-user-spec
  (ds/spec {:name ::post-user
            :spec {:username string?
                   :password string?
                   :name string?
                   :email string? }}))

(def post-login-spec
  (ds/spec {:name ::post-login
            :spec {:username string?
                   :password string?}}))

(def get-subscriptions-spec
  (ds/spec {:name ::get-subscriptions
            :spec {:type (s/spec #{"channel" "direct"})
                   :team-uuid string?}}))

(def post-generate-direct-spec
  (ds/spec {:name ::post-channels-generate
            :spec {:team-uuid string?
                   :other-user string?}}))
