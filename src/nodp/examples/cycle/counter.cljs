(ns nodp.examples.cycle.counter
  (:require [nodp.helpers :as helpers]
            [nodp.helpers.clojure.core :as core]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.unit :as unit]))

(def increment
  (frp/event))

(def decrement
  (frp/event))

(defn counter-component
  [total]
  [:div
   [:button {:on-click #(increment unit/unit)}
    "Increment"]
   [:button {:on-click #(decrement unit/unit)}
    "Decrement"]
   [:p (str "Counter: " total)]])

(def counter
  (->> (helpers/<> (helpers/<$> (constantly 1) increment)
                   (helpers/<$> (constantly -1) decrement))
       core/+
       (frp/stepper 0)
       (helpers/<$> counter-component)))
