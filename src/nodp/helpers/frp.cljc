(ns nodp.helpers.frp
  (:refer-clojure :exclude [stepper time transduce])
  (:require [nodp.helpers.derived :as derived]
            [nodp.helpers.primitives.behavior :as behavior :include-macros true]
            [nodp.helpers.primitives.event :as event]
            [nodp.helpers.io :as io]))

#?(:clj (defmacro register
          [& body]
          `(behavior/register ~body)))

(def restart
  (comp (fn [_]
          (def time
            behavior/time))
        behavior/restart))

(def event
  io/event)

(def behavior
  derived/behavior)

(declare time)

(def stepper
  behavior/stepper)

(def transduce
  event/transduce)

(def activate
  event/activate)

(def on
  io/on)

(def combine
  derived/combine)

(def mean
  derived/mean)

(def switcher
  derived/switcher)
