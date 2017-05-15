(ns nodp.core
  (:require [bidi.bidi :as bidi]
            [reagent.core :as r]
            [nodp.examples.index :as index]
            [nodp.examples.drag-n-drop :as drag-n-drop]
            [nodp.examples.letter-count :as letter-count]
            [nodp.examples.simple-data-binding :as simple-data-binding]
            [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.location :as location]))

(frp/restart)

(def index
  (frp/behavior index/index-component))

(def drag-n-drop
  (frp/behavior drag-n-drop/drag-n-drop-component))

(def length
  (frp/event))

(def letter-count
  (->> length
       (helpers/<$> (partial str "length: "))
       (frp/stepper "Start Typing!")
       (helpers/<$> (letter-count/letter-count-component length))))

(def first-name
  (frp/event))

(def last-name
  (frp/event))

(def full-name
  ((helpers/lift-a 3 str)
    (frp/stepper "" first-name)
    (frp/behavior " ")
    (frp/stepper "" last-name)))

(def simple-data-binding
  (helpers/<$>
    (simple-data-binding/simple-data-binding-component {:first-name first-name
                                                        :last-name  last-name})
    full-name))

(def app
  (helpers/=<< (comp {:index               index
                      :drag-n-drop         drag-n-drop
                      :letter-count        letter-count
                      :simple-data-binding simple-data-binding}
                     :handler
                     (partial bidi/match-route index/route))
               location/pathname))

(frp/on (partial (helpers/flip r/render) (js/document.getElementById "app"))
        app)

(frp/activate)
