(ns nodp.core
  (:require [bidi.bidi :as bidi]
            [reagent.core :as r]
            [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.history :as history]
            [nodp.helpers.location :as location]))

(frp/restart)

(def route
  ["/" {""         :index
        "absolute" :absolute}])

(def index
  [:ul
   [:a {:href     (bidi/path-for route :absolute)
        :on-click (fn [event*]
                    (.preventDefault event*)
                    (history/push-state {} {} (bidi/path-for route :absolute)))}
    [:li "absolute"]]])

(def app
  (helpers/<$> (fn [pathname*]
                 (case (:handler (bidi/match-route route pathname*))
                   :absolute [:div]
                   index))
               location/pathname))

(frp/on (partial (helpers/flip r/render) (js/document.getElementById "app"))
        app)

(frp/activate)
