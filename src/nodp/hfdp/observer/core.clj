(ns nodp.hfdp.observer.core
  (:require [clojure.string :as str]
            [aid.core :as aid]
            [beicon.core :as rx]
            [nodp.helpers :as helpers])
  (:import (rx.functions FuncN)
           (rx Observable)))

(def measurement-stream
  (rx/subject))

(def pressure
  (rx/map :pressure measurement-stream))

(def get-delta
  (comp (partial apply -)
        reverse))

(def delta-stream
  (->> pressure
       (rx/buffer 2 1)
       (rx/map get-delta)))

(defn forecast
  [delta]
  (aid/casep delta
             pos? "Improving weather on the way!"
             zero? "More of the same"
             "Watch out for cooler, rainy weather"))

(def forecast-stream
  (rx/map forecast delta-stream))

(helpers/printstream forecast-stream)

(def temperature
  (rx/map :temperature measurement-stream))

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
  (aid/casep x
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

(defn get-weather
  [{:keys [temperature humidity]}]
  (str "Current conditions: "
       temperature
       "F degrees and "
       humidity
       "% humidity"))

(def weather-stream
  (rx/map get-weather measurement-stream))

(helpers/printstream weather-stream)

(def measurements
  [{:temperature 80
    :humidity    65
    :pressure    (rationalize 30.4)}
   {:temperature 82
    :humidity    70
    :pressure    (rationalize 29.2)}
   {:temperature 78
    :humidity    90
    :pressure    (rationalize 29.2)}])

(run! (partial rx/push! measurement-stream)
      measurements)
