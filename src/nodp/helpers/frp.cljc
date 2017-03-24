(ns nodp.helpers.frp
  (:require [nodp.helpers :as helpers]
            [nodp.helpers.effect :as effect]
            [nodp.helpers.primitives.event :as event]))

(def restart
  helpers/restart)

(def event
  event/event)

(def activate
  event/activate)

(def on
  effect/on)
