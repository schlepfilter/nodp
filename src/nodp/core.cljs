(ns ^:figwheel-always nodp.core
  (:require [bidi.bidi :as bidi]
            [reagent.core :as r]
            [nodp.examples.drag-n-drop :as drag-n-drop]
            [nodp.examples.index :as index]
            [nodp.examples.letter-count :as letter-count]
            [nodp.examples.simple-data-binding :as simple-data-binding]
            [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.location :as location]))

(def drag-n-drop
  (frp/behavior drag-n-drop/drag-n-drop-component))

(def app
  (helpers/=<<
    (comp {:index               index/index
           :drag-n-drop         drag-n-drop
           :letter-count        letter-count/letter-count
           :simple-data-binding simple-data-binding/simple-data-binding}
          :handler
          (partial bidi/match-route index/route))
    location/pathname))

(frp/on (partial (helpers/flip r/render) (js/document.getElementById "app"))
        app)

(frp/activate)
