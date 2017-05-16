(ns ^:figwheel-always nodp.examples.drag-n-drop
  (:require [nodp.helpers.clojure.core :as core]
            [nodp.helpers :as helpers]
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

(def movement
  (->> drag
       (frp/snapshot window/mousemove)
       (core/filter second)
       (helpers/<$> first)))

(def left
  (frp/stepper 0 (core/+ (helpers/<$> :movement-x movement))))

(def top
  (frp/stepper 0 (core/+ (helpers/<$> :movement-y movement))))

(def origin
  ((helpers/lift-a 2 (fn [left* top*]
                       {:left left*
                        :top  top*}))
    left
    top))

(defn drag-n-drop-component
  [{:keys [left top]}]
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
                          :left                left
                          :position            "absolute"
                          :top                 top
                          :width               200}}
    "Drag Me!"]
   [:h1 "Drag and Drop Example"]
   [:p "Example to show coordinating events to perform drag and drop"]])

(def drag-n-drop
  (helpers/<$> drag-n-drop-component origin))
