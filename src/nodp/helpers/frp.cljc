(ns nodp.helpers.frp
  (:require [nodp.helpers :as helpers]
            [nodp.helpers.primitives.event :as event]))

(def initialize
  helpers/initialize)

(def event
  event/event)
