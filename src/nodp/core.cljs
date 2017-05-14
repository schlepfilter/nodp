(ns nodp.core
  (:require [bidi.bidi :as bidi]
            [reagent.core :as r]
            [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.history :as history]
            [nodp.helpers.location :as location]))

(frp/restart)

(def route
  ["/" {""            :index
        "lettercount" :letter-count}])

(def index
  [:ul
   [:a {:href     (bidi/path-for route :letter-count)
        :on-click (fn [event*]
                    (.preventDefault event*)
                    (history/push-state {}
                                        {}
                                        (bidi/path-for route :letter-count)))}
    [:li "lettercount"]]])

(def letter-count
  [:div
   [:h1 "Letter Counting Example"]
   [:p "Example to show getting the current length of the input."]
   [:div
    [:p
     "Text buffer: "
     [:input]]
    [:p "Start Typing!"]]])

(def app
  (helpers/<$> (comp {:index        index
                      :letter-count letter-count}
                     :handler
                     (partial bidi/match-route route))
               location/pathname))

(frp/on (partial (helpers/flip r/render) (js/document.getElementById "app"))
        app)

(frp/activate)
