(ns nodp.helpers.history
  (:require [cemerick.url :as url]
            [nodp.helpers.frp :as frp]))

(def pushstate
  (frp/->Event ::pushstate))

(frp/register
  (frp/redef pushstate
             (frp/event)))

(defn push-state
  [state title url-string]
  (js/history.pushState state title url-string)
  (pushstate {:location {:pathname (:path (url/url url-string))}}))
