(ns nodp.helpers.frp
  (:refer-clojure :exclude [stepper])
  (:require [nodp.helpers.derived.behavior :as derived-behavior]
            [nodp.helpers.primitives.behavior :as behavior]
            [nodp.helpers.primitives.event :as event]
            [nodp.helpers.io :as io]))

(def restart
  behavior/restart)

(def activate
  event/activate)

(def event
  io/event)

(def behavior
  derived-behavior/behavior)

(def switcher
  behavior/switcher)

(def stepper
  derived-behavior/stepper)
