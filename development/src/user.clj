(ns user
  (:require
   [com.howard.uchat.backend.api-server.core :as core]
   [clojure.tools.deps.alpha.repl :refer [add-libs]]
   [ragtime.next-jdbc :as ragtime]
   [ragtime.repl :as repl]
   [next.jdbc :as jdbc]
   [next.jdbc.sql :as sql]
   [next.jdbc.connection :as connection]
   [clj-time.core :as time]
   )
  (:import (com.zaxxer.hikari HikariDataSource)
           (org.postgresql.jdbc PgConnection)))
;; TODO: should use interface instead 
(core/start-server!)

