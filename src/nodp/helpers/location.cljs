(ns nodp.helpers.location
  (:require [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp :include-macros true]
            [nodp.helpers.history :as history]
            [nodp.helpers.window :as window]))

(def pathname
  (frp/->Behavior ::pathname))

(frp/register
  (frp/redef pathname
             (->> (helpers/<> window/popstate history/pushstate)
                  (helpers/<$> (comp :pathname
                                     :location))
                  (frp/stepper js/location.pathname))))
