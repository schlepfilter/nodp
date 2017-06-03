(ns nodp.helpers.location
  (:require [help]
            [nodp.helpers.history :as history]
            [nodp.helpers.primitives.behavior
             :as behavior
             :include-macros true]
            [nodp.helpers.window :as window]))

(def pathname
  (behavior/->Behavior ::pathname))

(behavior/register
  (behavior/redef pathname
                  (->> (help/<> window/popstate history/pushstate)
                       (help/<$> (comp :pathname
                                       :location))
                       (behavior/stepper js/location.pathname))))
