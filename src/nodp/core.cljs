(ns nodp.core
  (:require [reagent.core :as r]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.location :as location]))

(frp/restart)

(frp/on (fn [pathname*]
          (r/render [:ul [:li "absolute"]] (js/document.getElementById "app")))
        location/pathname)

(frp/activate)
