(ns nodp.examples.cycle.bmi-naive
  (:require [nodp.helpers.frp :as frp :include-macros true]
            [nodp.helpers :as helpers]))

(def weight-event
  (frp/event))

(def height-event
  (frp/event))

(def weight-behavior
  (frp/stepper 70 weight-event))

(def height-behavior
  (frp/stepper 170 height-event))

(def bmi
  (frp/transparent (int (/ weight-behavior
                           (js/Math.pow (/ height-behavior 100) 2)))))

(defn bmi-naive-component
  [weight height bmi*]
  [:div
   [:div "Weight " (str weight) "kg"
    [:input {:max       140
             :min       40
             :on-change (fn [event*]
                          (-> event*
                              .-target.value
                              weight-event))
             :type      "range"
             :value     weight}]]
   [:div "Height " (str height) "cm"
    [:input {:max       210
             :min       140
             :on-change (fn [event*]
                          (-> event*
                              .-target.value
                              height-event))
             :type      "range"
             :value     height}]]
   [:h2 "BMI is " bmi*]])

(def bmi-naive
  ((helpers/lift-a bmi-naive-component) weight-behavior height-behavior bmi))
