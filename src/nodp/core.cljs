(ns nodp.core
  (:require [bidi.bidi :as bidi]
            [com.rpl.specter :as s]
            [help]
            [reagent.core :as r]
            [nodp.examples.index :as index]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.location :as location]))

(def app
  (help/=<< (comp (s/setval :index index/index index/route-function)
                  :handler
                  (partial bidi/match-route index/route))
            location/pathname))

(frp/on (partial (help/flip r/render) (js/document.getElementById "app"))
        app)

(frp/activate)
