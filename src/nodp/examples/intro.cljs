(ns ^:figwheel-always nodp.examples.intro
  (:require [nodp.helpers.frp :as frp]
            [ajax.core :refer [GET POST]]))

(def response
  (frp/event))

(def intro
  (frp/behavior
    [:div
     [:h2 "Who to follow"]
     [:a {:href     "#"
          :on-click (fn [event*]
                      (.preventDefault event*)
                      (GET (str "https://api.github.com/users?since=" 0)
                           {:handler response}))}
      "refresh"]]))
