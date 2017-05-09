(ns nodp.hfdp.observer.synchronization
  (:require [nodp.helpers.frp :as frp]
            [nodp.helpers :as helpers]))

(frp/restart)

(def measurement
  (frp/event))

(def temperature
  (helpers/<$> :temperature measurement))

(frp/activate)

(run! measurement [{:temperature 80
                    :humidity    65
                    :pressure    (rationalize 30.4)}
                   {:temperature 82
                    :humidity    70
                    :pressure    (rationalize 29.2)}
                   {:temperature 78
                    :humidity    90
                    :pressure    (rationalize 29.2)}])
