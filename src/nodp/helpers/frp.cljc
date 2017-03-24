(ns nodp.helpers.frp
  (:require [nodp.helpers :as helpers]
            [nodp.helpers.primitives.event :as event]))

(def restart
  helpers/restart)

(def event
  event/event)

(def activate
  event/activate)
