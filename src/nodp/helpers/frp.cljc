(ns nodp.helpers.frp
  (:require [nodp.helpers.primitives.behavior :as behavior]
            [nodp.helpers.primitives.event :as event]
            [nodp.helpers.io :as io]))

(def restart
  behavior/restart)

(def activate
  event/activate)

(def event
  io/event)
