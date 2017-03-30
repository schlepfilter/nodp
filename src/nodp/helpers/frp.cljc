(ns nodp.helpers.frp
  (:require [nodp.helpers :as helpers]
            [nodp.helpers.io :as io]
            [nodp.helpers.primitives.event :as event]))

(def restart
  helpers/restart)

(def activate
  event/activate)

(def event
  io/event)

(def on
  io/on)
