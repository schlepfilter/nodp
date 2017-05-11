(ns nodp.hfdp.observer.synchronization
  (:require [clojure.string :as str]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.clojure.core :as core]
            [nodp.helpers :as helpers]
            [nodp.hfdp.observer.core :as observer-core]))

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

(def statistics
  (frp/transparent (->> (vector mean-temperature
                                max-temperature
                                min-temperature)
                        (str/join "/")
                        (str "Avg/Max/Min temperature = "))))

(frp/on println statistics)

(frp/activate)

(run! measurement-event observer-core/measurements)
