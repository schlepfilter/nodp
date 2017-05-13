(ns nodp.core
  (:require [reagent.core :as r]
            [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.history :as history]
            [nodp.helpers.location :as location]))

(frp/restart)

(def app
  (helpers/<$> (fn [pathname*]
                 [:ul
                  [:a {:on-click #(history/push-state {} {} "/absolute")}
                   [:li "absolute"]]])
               location/pathname))

(frp/on (partial (helpers/flip r/render) (js/document.getElementById "app"))
        app)

(frp/activate)
