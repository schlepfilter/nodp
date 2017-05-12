(ns nodp.helpers.history
  (:require [nodp.helpers.frp :as frp]))

(frp/register
  (def pushstate
    (frp/event)))

(defn push-state
  [state title url]
  (js/history.pushState state title url)
  ;TODO get pathname
  (pushstate {:location {:pathname url}}))
