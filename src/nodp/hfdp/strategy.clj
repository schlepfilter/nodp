(ns nodp.hfdp.strategy
  (:require [nodp.helpers :as helpers]))

(def quack
  (helpers/print-constantly "Quack"))

(def fly-rocket-powered
  (helpers/print-constantly "I'm flying with a rocket"))

(def fly-no-way
  (helpers/print-constantly "I can't fly"))

(defn funcall
  [f]
  (f))

(funcall quack)

(funcall fly-no-way)

(funcall fly-rocket-powered)
