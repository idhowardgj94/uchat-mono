(ns com.howard.uchat.backend.api-server.core-test
  (:require
   [clojure.test :refer [deftest is use-fixtures] :as t]
   [com.howard.uchat.backend.api-server.system]
   [integrant.core :as ig]
   [com.howard.uchat.backend.api-server.core :as subject]))

(def system {:db/jdbc {:jdbc-url {:host "localhost"
                                  :dbtype "postgres"
                                  :dbname "uchat_test"
                                  :useSSL false}
                       :username "postgres"
                       :password "postgres"}
             :db/migrations {:run? true
                             :rollback? true
                             :db-path "migrations"
                             :db-pool (ig/ref :db/jdbc)}
             :server/restful {:port 4003 :db-pool (ig/ref :db/jdbc)
                              :default-team? true}
             :server/websocket false})

(defn system-fixtures [f]
  (let [s (ig/init system)]
    (f)
    (ig/halt! s)))

(deftest home-test
  (is (= true
         true)))
