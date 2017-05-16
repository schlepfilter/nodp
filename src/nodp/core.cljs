(ns ^:figwheel-always nodp.core
  (:require [bidi.bidi :as bidi]
            [reagent.core :as r]
            [com.rpl.specter :as s]
            [nodp.examples.index :as index]
            [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.location :as location]))

(def app
  (helpers/=<<
    (comp (s/setval :index index/index index/route-function)
          :handler
          (partial bidi/match-route index/route))
    location/pathname))

(frp/on (partial (helpers/flip r/render) (js/document.getElementById "app"))
        app)

(frp/activate)
