(ns nodp.helpers.frp
  (:refer-clojure :exclude [stepper transduce])
  (:require [nodp.helpers.io :as io]
            [nodp.helpers.primitives.behavior :as behavior]
            [nodp.helpers.primitives.event :as event]
            [nodp.helpers.derived.behavior :as derived-behavior]))

(def restart
  event/restart)

(def activate
  event/activate)

(def transduce
  event/transduce)

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
