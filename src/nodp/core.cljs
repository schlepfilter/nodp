(ns nodp.core
  (:require [bidi.bidi :as bidi]
            [reagent.core :as r]
            [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.history :as history]
            [nodp.helpers.location :as location]))

(frp/restart)

(def route
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

(defn letter-count-component
  [message]
  [:div
   [:h1 "Letter Counting Example"]
   [:p "Example to show getting the current length of the input."]
   [:div [:p
          "Text buffer: "
          [:input {:on-change (fn [event*]
                                (-> event*
                                    .-target.value.length
                                    length))}]]
    [:p message]]])

(def letter-count-behavior
  (->> length
       (helpers/<$> (partial str "length: "))
       (frp/stepper "Start Typing!")
       (helpers/<$> letter-count-component)))

(def simple-data-binding
  (frp/behavior [:div]))

(def app
  (helpers/=<<
    (comp {:index               index
           :letter-count        letter-count-behavior
           :simple-data-binding simple-data-binding}
          :handler
          (partial bidi/match-route route))
    location/pathname))

(frp/on (partial (helpers/flip r/render) (js/document.getElementById "app"))
        app)

(frp/activate)
