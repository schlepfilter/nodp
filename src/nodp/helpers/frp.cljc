(ns nodp.helpers.frp
  (:require [nodp.helpers :as helpers]
            [nodp.helpers.io :as io]
            [nodp.helpers.primitives.event :as event]
            [nodp.helpers.derived.behavior :as derived-behavior]))

(def restart
  helpers/restart)

(def activate
  event/activate)

(def behavior
  derived-behavior/behavior)

(def event
  io/event)

(def on
  io/on)
