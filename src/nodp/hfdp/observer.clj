(ns nodp.hfdp.observer
  (:require [clojure.string :as str]
            [beicon.core :as rx]
            [incanter.distributions :as distributions]
            [nodp.helpers :as helpers])
  (:import (rx.functions FuncN)
           (rx Observable)))

(def measurement
  (-> (rx/of {:temperature 80
              :humidity    65
              :pressure    (rationalize 30.4)}
             {:temperature 82
              :humidity    70
              :pressure    (rationalize 29.2)}
             {:temperature 78
              :humidity    90
              :pressure    (rationalize 29.2)})
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

(def rx-average
  (partial rx/scan (comp distributions/mean
                         vector)))

(def max-temperature
  (rx-max temperature))

(def min-temperature
  (rx-min temperature))

(def average-temperature
  (rx-average temperature))

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
                  average-temperature
                  max-temperature
                  min-temperature))

(helpers/printstream statistic-stream)

(defn- get-now
  [{:keys [temperature humidity]}]
  (str "Current conditions: "
       temperature
       "F degrees and "
       humidity
       "% humidity"))

(def now-stream
  (rx/map get-now measurement))

(helpers/printstream now-stream)

(rx/connect! measurement)
