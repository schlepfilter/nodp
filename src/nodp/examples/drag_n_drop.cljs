(ns ^:figwheel-always nodp.examples.drag-n-drop
  (:require [nodp.helpers.frp :as frp]))

(def black "hsl(0, 0%, 0%)")

(def white "hsl(0, 0%, 100%)")

(def drag-n-drop-component
  [:div
   [:div {:style {:background-image    "url(/img/logo.png)"
                  :background-repeat   "no-repeat"
                  :background-position "center"
                  :background-color    black
                  :color               white
                  :height              200
                  :width               200}}
    "Drag Me!"]
   [:h1 "Drag and Drop Example"]
   [:p "Example to show coordinating events to perform drag and drop"]])

(def drag-n-drop
  (frp/behavior drag-n-drop-component))
