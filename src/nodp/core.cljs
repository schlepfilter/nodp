(ns nodp.core
  (:require [reagent.core :as r]
            [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.history :as history]
            [nodp.helpers.location :as location]))

(frp/restart)

(def index
  [:ul
   [:a {:href     "/absolute"
        :on-click (fn [event*]
                    (.preventDefault event*)
                    (history/push-state {} {} "/absolute"))}
    [:li "absolute"]]])

(def app
  (helpers/<$> (fn [pathname*]
                 (case pathname*
                   "/absolute" [:div]
                   index))
               location/pathname))

(frp/on (partial (helpers/flip r/render) (js/document.getElementById "app"))
        app)

(frp/activate)
