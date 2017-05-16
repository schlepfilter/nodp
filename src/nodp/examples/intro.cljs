(ns ^:figwheel-always nodp.examples.intro
  (:require [nodp.helpers.frp :as frp]))

(def intro
  (frp/behavior [:div
                 [:h2 "Who to follow"]
                 [:a {:href     "#"
                      :on-click (fn [event*]
                                  (.preventDefault event*))}
                  "refresh"]]))
