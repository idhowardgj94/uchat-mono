(ns com.howard.uchat.backend.api-server.spec
  (:require
   [spec-tools.data-spec :as ds]))

(def post-user-spec
  (ds/spec {:name ::post-user-spec
            :spec {:username string?
                   :password string?
                   :name string?
                   :email string? }}))
