(ns nodp.hfdp.observer.synchronization
  (:require [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp]))

(frp/restart)

(def measurement
  (frp/event))

(def temperature
  (helpers/<$> :temperature measurement))

;TODO implement weather

(frp/activate)
