(ns user
  (:require
   [clojure.tools.deps.alpha.repl :refer [add-libs]]
   [next.jdbc :as jdbc]
   [next.jdbc.connection :as connection])
  (:import (com.zaxxer.hikari HikariDataSource)))
(comment
  (add-libs '{integrant/integrant {:mvn/version "0.8.1"}})
  (add-libs '{buddy/buddy-auth {:mvn/version "3.0.323"}})
;; this is connection pooling
  (add-libs '{com.zaxxer/HikariCP {:mvn/version "3.3.1"}})
  (add-libs '{ring/ring-defaults {:mvn/version "0.3.4"}
              ring/ring-core {:mvn/version "1.8.2"}
              ring/ring-json {:mvn/version "0.5.1"}
              ring/ring-jetty-adapter {:mvn/version "1.8.2"}
              buddy/buddy-auth {:mvn/version "3.0.323"}
              ring/ring-devel {:mvn/version "1.10.0"}
              compojure/compojure {:mvn/version "1.7.0"}
              http-kit/http-kit {:mvn/version "2.7.0"}
              })
;; this is nextjdbc
  (add-libs '{com.github.seancorfield/next.jdbc {:mvn/version "1.3.883"}})
;; posgres driver
  (add-libs '{org.postgresql/postgresql {:mvn/version "42.2.10"}}))

(def config
  {:uchat/db-connection {:dbtype "postgres"
                        :dbname "uchat"
                        :useSSL "false"}
   :uchat/db-auth {:username "postgres" :password ""}})

(def db-pool
  (connection/->pool HikariDataSource
                     {:jdbcUrl
                      (connection/jdbc-url {:dbtype "postgres" :dbname "uchat" :useSSL false})
                      :username "postgres" :password ""}))

(jdbc/execute! db-pool ["select * from users"])


(require
 '[compojure.core :refer :all]
 '[ring.middleware.json :refer [wrap-json-response]]
 '[ring.middleware.reload :refer [wrap-reload]]
 '[ring.middleware.resource :refer [wrap-resource]]
 '[ring.util.response :refer [resource-response]]
 '[ring.middleware.defaults :refer :all]
 '[org.httpkit.server :as hk-server])

(defn wrap-cors
  [handler]
  (fn [request]
    (assoc-in (handler request) [:headers "Access-Control-Allow-Origin"] "*")))

(defroutes app-routes
  (GET "/" [] "hello, world"))


(defonce server (atom nil))

(reset! server
        (hk-server/run-server
         (-> #'app-routes
             (wrap-reload)
             (wrap-cors)
             (wrap-resource "public")
             (wrap-defaults api-defaults)
             (wrap-json-response))
         {:join? false :port 4000}))

(@server)
