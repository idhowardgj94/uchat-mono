(ns com.howard.uchat.backend.database.spec
  (:require
   ;; [clojure.spec.alpha :as s]
   [spec-tools.data-spec :as ds]))
; TODO 
(def database-option-spec
  "spec for database opts"
  (ds/spec {:name ::database
            :spec {:jdbcUrl string?
                   (ds/opt :username) string?
                   (ds/opt :password) string?}}))

;; (ns-unalias *ns* 's)
