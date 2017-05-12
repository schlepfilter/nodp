(ns nodp.helpers.location
  (:require [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp :include-macros true]
            [nodp.helpers.history :as history]
            [nodp.helpers.window :as window]))

(declare pathname)

(frp/register
  (def pathname
    (->> (helpers/<> window/popstate history/pushstate)
         (helpers/<$> (comp :pathname
                            :location))
         (frp/stepper js/location.pathname))))
