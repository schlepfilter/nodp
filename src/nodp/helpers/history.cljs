(ns nodp.helpers.history
  (:require [nodp.helpers.frp :as frp]))

(def pushstate
  (frp/->Event ::pushstate))

(frp/register
  (frp/redef pushstate
             (frp/event)))

(defn push-state
  [state title url]
  (js/history.pushState state title url)
  ;TODO get pathname
  (pushstate {:location {:pathname url}}))
