(ns nodp.hfdp.adapter
  (:require [nodp.helpers :as helpers]))

(def turkey-quack
  (helpers/print-constantly "Gobble gobble"))

(def duck-quack
  turkey-quack)

(def turkey-fly
  (helpers/print-constantly "I'm flying a short distance"))

(defn duck-fly
  []
  (dotimes [_ 5] (turkey-fly)))

(duck-quack)

(duck-fly)
