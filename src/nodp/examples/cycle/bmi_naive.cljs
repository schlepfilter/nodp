(ns nodp.examples.cycle.bmi-naive
  (:require [nodp.helpers.frp :as frp :include-macros true]))

(def weight-event
  (frp/event))

(def height-event
  (frp/event))

(def weight-behavior
  (frp/stepper 70 weight-event))

(def height-behavior
  (frp/stepper 170 height-event))

(def bmi
  ;TODO thread forms
  (frp/transparent (int (/ weight-behavior
                           (js/Math.pow (/ height-behavior 100) 2)))))

(defn weight-component
  [weight]
  [:div "Weight " (str weight) "kg"
   [:input {:max       140
            :min       40
            :on-change (fn [event*]
                         (-> event*
                             .-target.value
                             weight-event))
            :type      "range"
            :value     weight}]])

(defn height-component
  [height]
  [:div "Height " (str height) "cm"
   [:input {:max       210
            :min       140
            :on-change (fn [event*]
                         (-> event*
                             .-target.value
                             height-event))
            :type      "range"
            :value     height}]])

(defn bmi-component
  [bmi*]
  [:h2 "BMI is " bmi*])

(def bmi-naive
  (frp/transparent (vector :div
                           (weight-component weight-behavior)
                           (height-component height-behavior)
                           (bmi-component bmi))))
