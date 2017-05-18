(ns nodp.helpers.history
  (:require [nodp.helpers.derived :as derived]
            [nodp.helpers.primitives.behavior
             :as behavior
             :include-macros true]
            [nodp.helpers.primitives.event :as event]))

(def pushstate
  (event/->Event ::pushstate))

(behavior/register
  (behavior/redef pushstate
                  (derived/event)))

(defn push-state
  [state title url-string]
  (js/history.pushState state title url-string)
  (pushstate {:location {:pathname js/location.pathname}}))
