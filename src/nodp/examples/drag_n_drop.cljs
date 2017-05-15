(ns ^:figwheel-always nodp.examples.drag-n-drop
  (:require [nodp.helpers.frp :as frp]))

(def drag-n-drop-component
  [:div])

(def drag-n-drop
  (frp/behavior drag-n-drop-component))
