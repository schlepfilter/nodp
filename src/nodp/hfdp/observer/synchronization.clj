(ns nodp.hfdp.observer.synchronization
  (:require [clojure.string :as str]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.clojure.core :as core]
            [nodp.helpers :as helpers]
            [nodp.hfdp.observer.core :as observer-core]))

(frp/restart)

(def measurement-event
  (frp/event))

(def pressure
  (helpers/<$> :pressure measurement-event))

(def delta
  (helpers/<$> (comp (partial apply -)
                     reverse)
               (frp/buffer 2 1 pressure)))

(def forecast-event
  (helpers/<$> observer-core/forecast delta))

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

(def weather
  (helpers/<$> observer-core/get-weather measurement-event))

(run! (partial frp/on println) [forecast-event statistics weather])

(frp/activate)

(run! measurement-event observer-core/measurements)
