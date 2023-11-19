(ns ^:figwheel-hooks com.howard.uchat.core
  (:require [com.howard.uchat.config :as config]
            [com.howard.uchat.routes :as routes]
            [com.howard.uchat.styles :as styl]
            [com.howard.uchat.use-cases.core-cases :as ccases]
            [com.howard.uchat.views.home :as views]
            [com.howard.uchat.api :as api]
            [goog.dom :as gdom]
            [react :as react]
            ["javascript-time-ago/locale/en" :as en]
            ["javascript-time-ago$default" :as time-ago]
            [re-frame.core :as re-frame]
            [cljs.spec.alpha :as s]
            [reagent.core :as rc]
            [reagent.dom.client :as rdc]))

;(def functional-compiler (reagent.core/create-compiler {:function-components true}))
;(reagent.core/set-default-compiler! functional-compiler)

(doto time-ago
  (.addLocale en))
(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    ;; Set check assert to true, will throw exception if assert fail, should disable in production.
    (s/check-asserts true)
    (println "dev mode")))

(defonce root (rdc/create-root (gdom/getElement "app")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (styl/inject-trace-styles js/document)
  (rdc/render root [:> react/StrictMode {} [#'views/main-panel]]))

(defn ^:after-load re-render []
  (mount-root))  

(defn ^:export init []
  (println "init again..")
  (api/axios-response-to-clj)
  (re-frame/dispatch-sync [::ccases/initialize-db])
  (dev-setup)
  (routes/app-routes)

  (mount-root))
