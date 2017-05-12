(ns nodp.core
  (:require [reagent.core :as r]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.history :as history]
            [nodp.helpers.location :as location]))

(frp/restart)

(frp/on (fn [pathname*]
          (r/render [:ul
                     [:a {:on-click #(history/push-state {} {} "/absolute")}
                      [:li "absolute"]]] (js/document.getElementById "app")))
        location/pathname)

(frp/activate)
