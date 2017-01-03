(ns nodp.hfdp.observer
  (:require [clojure.string :as str]
            [beicon.core :as rx]
            [incanter.distributions :as distributions]
            [nodp.helpers :as helpers])
  (:import (rx.functions FuncN)))

(def measurement-stream
  (rx/of {:temperature 80
          :humidity    65
          :pressure    (rationalize 30.4)}
         {:temperature 82
          :humidity    70
          :pressure    (rationalize 29.2)}
         {:temperature 78
          :humidity    90
          :pressure    (rationalize 29.2)}))

(def pressure-stream
  (rx/map :pressure measurement-stream))

(def delta-stream
  (->> pressure-stream
       (rx/buffer 2 1)
       (rx/map (comp (partial apply -)
                     reverse))))

(defn- forecast
  [delta]
  (helpers/casep delta
                 pos? "Improving weather on the way!"
                 zero? "More of the same"
                 "Watch out for cooler, rainy weather"))

(def forecast-stream
  (rx/map forecast delta-stream))

(def printstream
  (partial (helpers/flip rx/on-next) println))

(printstream forecast-stream)

(def temperature-stream
  (rx/map :temperature measurement-stream))

(def rx-max
  (partial rx/scan max))

(def rx-min
  (partial rx/scan min))

(def rx-average
  (partial rx/scan (comp distributions/mean
                         vector)))

(def max-stream
  (rx-max temperature-stream))

(def min-stream
  (rx-min temperature-stream))

(def average-stream
  (rx-average temperature-stream))

(defn- rxfnn
  ^FuncN [f]
  (reify FuncN
    (call [_ objs]
      (apply f objs))))

(defmacro if-observable?
  [x then else]
  `(helpers/casep ~x
                  rx/observable? ~then
                  ~else))

(defn- with-latest-from
  [x & more]
  (.withLatestFrom (last more)
                   (if-observable? x
                                   more
                                   (drop-last more))
                   (-> (if-observable? x
                                       vector
                                       x)
                       rxfnn)))

;This is harder to read.
;(defn- with-latest-from
;  [x & more]
;  (apply (partial (helpers/functionize .withLatestFrom) (last more))
;         (s/transform s/LAST rxfnn
;           (if (rx/observable? x)
;            [more vector]
;            [(drop-last more) x]))))

(def statistic-stream
  (with-latest-from (comp str/join
                          (partial interleave
                                   ["Avg/Max/Min temperature = " "/" "/"])
                          vector)
                    max-stream
                    min-stream
                    average-stream))

(printstream statistic-stream)

(defn- get-current
  [{:keys [temperature humidity]}]
  (str "Current conditions: "
       temperature
       "F degrees and "
       humidity
       "% humidity"))

(def current-stream
  (rx/map get-current measurement-stream))

(printstream current-stream)
