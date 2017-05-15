(ns nodp.helpers.history
  (:require [nodp.helpers.frp :as frp]
            [nodp.helpers.primitives.behavior
             :as behavior
             :include-macros true]))

(def pushstate
  (frp/->Event ::pushstate))

(behavior/register
  (frp/redef pushstate
             (frp/event)))

(defn push-state
  [state title url-string]
  (js/history.pushState state title url-string)
  (pushstate {:location {:pathname js/location.pathname}}))
