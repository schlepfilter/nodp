(ns ^:figwheel-always nodp.examples.drag-n-drop
  (:require [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.unit :as unit]
            [nodp.helpers.window :as window]))

(def black "hsl(0, 0%, 0%)")

(def white "hsl(0, 0%, 100%)")

(def offset
  (frp/event))

(def client
  (frp/event))

(def drag
  (frp/stepper false (helpers/<> (helpers/<$> (constantly true) offset)
                                 (helpers/<$> (constantly false) client))))

(def drag-n-drop-component
  [:div
   [:div {:on-mouse-down (fn [_]
                           (offset unit/unit))
          :on-mouse-up   (fn [_]
                           (client unit/unit))
          :style         {:background-image    "url(/img/logo.png)"
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
