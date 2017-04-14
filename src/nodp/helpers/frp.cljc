(ns nodp.helpers.frp
  (:refer-clojure :exclude [stepper time transduce])
  (:require [nodp.helpers.io :as io]
            [nodp.helpers.primitives.behavior :as behavior]
            [nodp.helpers.primitives.event :as event]
            [nodp.helpers.derived.behavior :as derived-behavior]))

(def restart
  behavior/restart)

(def activate
  behavior/activate)

(def transduce
  event/transduce)

(def time
  behavior/time)

(def switcher
  behavior/switcher)

(def behavior
  derived-behavior/behavior)

(def stepper
  derived-behavior/stepper)

(def event
  io/event)

(def on
  io/on)
