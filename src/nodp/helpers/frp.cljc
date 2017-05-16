(ns ^:figwheel-always nodp.helpers.frp
  (:refer-clojure :exclude [stepper time transduce])
  (:require [nodp.helpers.derived :as derived :include-macros true]
            [nodp.helpers.io :as io]
            [nodp.helpers.primitives.behavior :as behavior :include-macros true]
            [nodp.helpers.primitives.event :as event]
    #?(:cljs [nodp.helpers.location])))

(def restart
  behavior/restart)

(def event
  io/event)

(def behavior
  derived/behavior)

(def time
  behavior/time)

(def stepper
  behavior/stepper)

(def time-transform
  behavior/time-transform)

(def transduce
  event/transduce)

(def snapshot
  event/snapshot)

(def activate
  event/activate)

(def on
  io/on)

(def combine
  derived/combine)

(defmacro transparent
  [expr]
  `(derived/transparent ~expr))

(def buffer
  derived/buffer)

(def mean
  derived/mean)

(def switcher
  derived/switcher)

(restart)
