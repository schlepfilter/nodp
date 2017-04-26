(ns nodp.helpers.frp
  (:refer-clojure :exclude [+ count drop max min stepper time transduce])
  (:require [nodp.helpers.io :as io]
            [nodp.helpers.derived.behavior
             :as derived-behavior
             :include-macros true]
            [nodp.helpers.derived.event :as derived-event]
            [nodp.helpers.primitives.behavior :as behavior]
            [nodp.helpers.primitives.event :as event]))

(declare time)

(def restart
  (comp (fn [_]
          (def time
            behavior/time))
        behavior/restart))

(def transduce
  event/transduce)

(def activate
  event/activate)

(def switcher
  behavior/switcher)

(def calculus
  behavior/calculus)

(def +
  derived-event/+)

(def count
  derived-event/count)

(def drop
  derived-event/drop)

(def max
  derived-event/max)

(def min
  derived-event/min)

#?(:clj (defmacro lifting
          [expr]
          `(derived-behavior/lifting ~expr)))

(def behavior
  derived-behavior/behavior)

(def stepper
  derived-behavior/stepper)

(def integral
  derived-behavior/integral)

(def derivative
  derived-behavior/derivative)

(def event
  io/event)

(def on
  io/on)
