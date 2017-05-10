(ns nodp.hfdp.observer.synchronization
  (:require [nodp.helpers.frp :as frp]
            [nodp.helpers.clojure.core :as core]
            [nodp.helpers :as helpers]))

(frp/restart)

(def measurement-event
  (frp/event))

(def temperature
  (helpers/<$> :temperature measurement-event))

(def mean-temperature
  (frp/mean temperature))

(def min-temperature
  (core/min temperature))

(def max-temperature
  (core/max temperature))

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
