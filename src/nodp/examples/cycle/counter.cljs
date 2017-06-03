(ns nodp.examples.cycle.counter
  (:require [help.core :as help]
            [help.unit :as unit]
            [nodp.helpers.clojure.core :as core]
            [nodp.helpers.frp :as frp]))

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
  (->> (help/<> (help/<$> (constantly 1) increment)
                (help/<$> (constantly -1) decrement))
       core/+
       (frp/stepper 0)
       (help/<$> counter-component)))
