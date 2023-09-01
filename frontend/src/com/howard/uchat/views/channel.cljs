(ns com.howard.uchat.views.channel
  (:require [com.howard.uchat.components.layout :refer [main-layout]]
            [com.howard.uchat.views.rooms :refer [room]]))


(defn channel
  "This is a channel layout."
  []
  [main-layout
   [room]])
