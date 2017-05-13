(ns nodp.helpers.history
  (:require [cemerick.url :as url]
            [nodp.helpers.frp :as frp]))

(def pushstate
  (frp/->Event ::pushstate))

(frp/register
  (frp/redef pushstate
             (frp/event)))

(def get-pathname
  (comp :path
        url/url))

(defn push-state
  [state title url-string]
  (->> url-string
       get-pathname
       (js/history.pushState state title))
  (pushstate {:location {:pathname (get-pathname url-string)}}))
