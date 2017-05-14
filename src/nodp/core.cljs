(ns nodp.core
  (:require [bidi.bidi :as bidi]
            [reagent.core :as r]
            [nodp.examples.letter-count :as letter-count]
            [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.history :as history]
            [nodp.helpers.location :as location]))

(def route
  ["/" {""                  :index
        "lettercount"       :letter-count
        "simpledatabinding" :simple-data-binding}])

(def route-keys
  [:letter-count :simple-data-binding])

(defn example
  [path]
  [:a {:href     path
       :on-click (fn [event*]
                   (.preventDefault event*)
                   (history/push-state {}
                                       {}
                                       path))}
   [:li path]])

(def index
  (->> route-keys
       (map (comp example
                  (partial bidi/path-for route)))
       (cons :ul)
       (into [])))

(def app
  (helpers/=<< (comp {:index        (frp/behavior index)
                      :letter-count letter-count/letter-count-behavior}
                     :handler
                     (partial bidi/match-route route))
               location/pathname))

(frp/on (partial (helpers/flip r/render) (js/document.getElementById "app"))
        app)

(frp/activate)
