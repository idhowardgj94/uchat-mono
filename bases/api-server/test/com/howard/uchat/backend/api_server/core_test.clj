(ns com.howard.uchat.backend.api-server.core-test
  (:require
   [clojure.test :refer [deftest is use-fixtures testing run-all-tests] :as t]
   [com.howard.uchat.backend.api-server.system]
   [integrant.core :as ig]
   [jsonista.core :as json]
   [com.howard.uchat.backend.tools.interface :as tools])
  (:import [com.howard.uchat.backend.tools.interface Client]))

(defn read-json
  [str]
  (json/read-value str json/keyword-keys-object-mapper))

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
(defn new-client
  []
  (tools/new-client "test" "test" {:port 4003}))

#_(-> (new-client)
  (.get_ "/"))
(defn system-fixtures [f]
  (let [s (ig/init system)]
    (f)
    (ig/halt! s)))


(use-fixtures :once system-fixtures)
(deftest test-call-home-and-received-200
  (let [res (-> (new-client) (.get_ "/"))]
    (is (= 200 (:status res)))))

(deftest test-register-a-user-and-return-200
  (let [res (-> (new-client)
                 (.post "/api/v1/register"
                        {:username "test"
                         :password "test"
                         :name "test"
                         :email "test@gmail.com"}))]
    (is (= "success"
           (-> res
               :body
               read-json
               :status)))))

(deftest test-api-v1-login
  (let [client (new-client)]
    (testing "should try to login and login failed."
      (try
        (-> client
            (.post "/api/v1/login"
                   {:username "wrong"
                    :password "wrong"}))
        (catch Exception e
          (is (= (-> (ex-data e) :status) 400))
          (is (= (-> (ex-data e)
                     :body
                     read-json
                     :message)
                 "Login failed, please check username and password.")))))
    (testing "should try login and login success"
      (let [res (-> client
                    (.post "/api/v1/login"
                           {:username "test"
                            :password "test"}))]
        (is (= (:status res) 200))))))

(comment "test server"
         (require '[clojure.tools.namespace.repl :refer [refresh refresh-all]])
         (refresh-all)
         
         (remove-ns (symbol (namespace ::t)))
         (def s (ig/init system))
         (ig/halt! s)
         (run-all-tests #"test-.*")
         (run-all-tests)
         ,)

