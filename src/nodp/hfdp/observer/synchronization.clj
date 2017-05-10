(ns nodp.hfdp.observer.synchronization
  (:require [nodp.helpers.frp :as frp]
            [nodp.helpers :as helpers]))

(frp/restart)

(def measurement-event
  (frp/event))

(def temperature
  (helpers/<$> :temperature measurement-event))

(def max-temperature
  (frp/max temperature))

(def mean-temperature
  (frp/mean temperature))

(frp/activate)

(def measurements [{:temperature 80
                    :humidity    65
                    :pressure    (rationalize 30.4)}
                   {:temperature 82
                    :humidity    70
                    :pressure    (rationalize 29.2)}
                   {:temperature 78
                    :humidity    90
                    :pressure    (rationalize 29.2)}])

(run! measurement-event measurements)
