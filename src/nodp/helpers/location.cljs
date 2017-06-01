(ns nodp.helpers.location
  (:require [nodp.helpers :as helpers]
            [nodp.helpers.history :as history]
            [nodp.helpers.primitives.behavior
             :as behavior
             :include-macros true]
            [nodp.helpers.window :as window]))

(def pathname
  (behavior/->Behavior ::pathname))

(behavior/register
  (behavior/redef pathname
                  (->> (helpers/<> window/popstate history/pushstate)
                       (helpers/<$> (comp :pathname
                                          :location))
                       (behavior/stepper js/location.pathname))))
