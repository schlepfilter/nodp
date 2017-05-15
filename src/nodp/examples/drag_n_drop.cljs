(ns ^:figwheel-always nodp.examples.drag-n-drop
  (:require [nodp.helpers.frp :as frp]))

(def drag-n-drop-component
  [:div
   [:h1 "Drag and Drop Example"]
   [:p "Example to show coordinating events to perform drag and drop"]])

(def drag-n-drop
  (frp/behavior drag-n-drop-component))
