(ns nodp.core
  (:require [bidi.bidi :as bidi]
            [reagent.core :as r]
            [nodp.examples.index :as index]
            [nodp.examples.letter-count :as letter-count]
            [nodp.examples.simple-data-binding :as simple-data-binding]
            [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.location :as location]))

(frp/restart)

(def index
  (frp/behavior index/index-component))

(def length
  (frp/event))

(def letter-count
  (->> length
       (helpers/<$> (partial str "length: "))
       (frp/stepper "Start Typing!")
       (helpers/<$> (letter-count/letter-count-component length))))

(def simple-data-binding
  (frp/behavior simple-data-binding/simple-data-binding-component))

(def app
  (helpers/=<< (comp {:index               index
                      :letter-count        letter-count
                      :simple-data-binding simple-data-binding}
                     :handler
                     (partial bidi/match-route index/route))
               location/pathname))

(frp/on (partial (helpers/flip r/render) (js/document.getElementById "app"))
        app)

(frp/activate)
