(ns nodp.helpers.frp
  (:require [nodp.helpers.primitives.event :as event]
            [nodp.helpers.io :as io]))

(def activate
  event/activate)

(def event
  io/event)
