(ns com.howard.uchat.backend.api-server.main
  (:require
   [com.howard.uchat.backend.api-server.system :as system]
   [integrant.core :as ig])
  (:gen-class))


(defn -main []
  (ig/init (system/read-system "system.edn")))
