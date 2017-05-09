(ns nodp.hfdp.observer.core
  (:require [clojure.string :as str]
            [beicon.core :as rx]
            [nodp.helpers :as helpers]
            [nodp.hfdp.observer.synchronization :as synchronization])
  (:import (rx.functions FuncN)
           (rx Observable)))

(def measurement
  (->> synchronization/measurements
       (apply rx/of)
       .publish))

(def pressure
  (rx/map :pressure measurement))

(def delta
  (->> pressure
       (rx/buffer 2 1)
       (rx/skip 1)
       (rx/map (comp (partial apply -)
                     reverse))))

(defn- forecast
  [delta]
  (helpers/casep delta
                 pos? "Improving weather on the way!"
                 zero? "More of the same"
                 "Watch out for cooler, rainy weather"))

(def forecast-stream
  (rx/map forecast delta))

(helpers/printstream forecast-stream)

(def temperature
  (rx/map :temperature measurement))

(def rx-max
  (partial rx/scan max))

(def rx-min
  (partial rx/scan min))

(def max-temperature
  (rx-max temperature))

(def min-temperature
  (rx-min temperature))

(defn- rxfnn
  ^FuncN [f]
  (reify FuncN
    (call [_ objs]
      (apply f objs))))

(defn- combine-latest
  [x & more]
  (helpers/casep x
                 rx/observable? (apply combine-latest vector x more)
                 (Observable/combineLatest more (rxfnn x))))

(def statistic-stream
  ;TODO use a glitch-free library
  (combine-latest (comp str/join
                        (partial interleave
                                 ["Avg/Max/Min temperature = " "/" "/"])
                        vector)
                  ;TODO combine average temperature
                  max-temperature
                  min-temperature))

(helpers/printstream statistic-stream)

(defn- get-weather
  [{:keys [temperature humidity]}]
  (str "Current conditions: "
       temperature
       "F degrees and "
       humidity
       "% humidity"))

(def weather-stream
  (rx/map get-weather measurement))

(helpers/printstream weather-stream)

(rx/connect! measurement)
