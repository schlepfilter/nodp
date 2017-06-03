(ns nodp.hfdp.observer.synchronization
  (:require [clojure.string :as str]
            [help]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.clojure.core :as core]
            [nodp.hfdp.observer.core :as observer-core]))

(frp/restart)

(def measurement-event
  (frp/event))

(def pressure
  (help/<$> :pressure measurement-event))

(def delta
  (->> pressure
       (frp/buffer 2 1)
       (help/<$> observer-core/get-delta)))

(def forecast-event
  (help/<$> observer-core/forecast delta))

(def temperature
  (help/<$> :temperature measurement-event))

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
  (help/<$> observer-core/get-weather measurement-event))

(run! (partial frp/on println) [forecast-event statistics weather])

(frp/activate)

(run! measurement-event observer-core/measurements)
