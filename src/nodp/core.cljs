(ns nodp.core
  (:require [bidi.bidi :as bidi]
            [reagent.core :as r]
            [nodp.examples.letter-count :as letter-count]
            [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.history :as history]
            [nodp.helpers.location :as location]))

(frp/restart)

(def route
  ;TODO use route-keywords
  ["/" {""                  :index
        "lettercount"       :letter-count
        "simpledatabinding" :simple-data-binding}])

(def route-keywords
  [:letter-count :simple-data-binding])

(defn example
  [path]
  [:a {:href     path
       :on-click (fn [event*]
                   (.preventDefault event*)
                   (history/push-state {} {} path))}
   [:li path]])

(def index
  (->> route-keywords
       (map (comp example
                  (partial bidi/path-for route)))
       (cons :ul)
       (into [])
       frp/behavior))

(def length
  (frp/event))

(def letter-count
  (->> length
       (helpers/<$> (partial str "length: "))
       (frp/stepper "Start Typing!")
       (helpers/<$> (letter-count/letter-count-component length))))

(def simple-data-binding
  (frp/behavior [:div]))

(def app
  (helpers/=<<
    (comp {:index               index
           :letter-count        letter-count
           :simple-data-binding simple-data-binding}
          :handler
          (partial bidi/match-route route))
    location/pathname))

(frp/on (partial (helpers/flip r/render) (js/document.getElementById "app"))
        app)

(frp/activate)
