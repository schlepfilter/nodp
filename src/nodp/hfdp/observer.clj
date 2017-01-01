(ns nodp.hfdp.observer
  (:require [clojure.string :as str]
            [beicon.core :as rx]
            [incanter.distributions :as distributions]
            [nodp.helpers :as helpers])
  (:import (rx.functions FuncN)))

(def subject (.toSerialized (rx/subject)))

(def pressure-stream
  (rx/map :pressure subject))

(def delta-stream
  (->> pressure-stream
       (rx/buffer 2 1)
       (rx/map (comp (partial apply -)
                     reverse))))

(defn call-pred
  ([_]
   true)
  ([pred expr]
   (pred expr)))

(defmacro casep
  [x & clauses]
  `(condp call-pred ~x
     ~@clauses))

(defn- forecast
  [delta]
  (casep delta
         pos? "Improving weather on the way!"
         zero? "More of the same"
         "Watch out for cooler, rainy weather"))

(def forecast-stream
  (rx/map forecast delta-stream))

(def printstream
  (partial (helpers/flip rx/subscribe) println))

(printstream forecast-stream)

(def temperature-stream
  (rx/map :temperature subject))

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

(defn- with-latest-from
  [x & more]
  (apply (partial (helpers/functionize .withLatestFrom) (last more))
         (if (rx/observable? x)
           [more (rxfnn vector)]
           [(drop-last more) (rxfnn x)])))

;This definition doesn't use functionize.
;(defn- with-latest-from
;  [x & more]
;  (if (rx/observable? x)
;    (.withLatestFrom (last more) more (rxfnn vector))
;    (.withLatestFrom (last more) (drop-last more) (rxfnn x))))

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
  (->> (rx/map get-current subject)))

(printstream current-stream)

(def push-measurement!
  (partial rx/push! subject))

(push-measurement! {:temperature 80
                    :humidity    65
                    :pressure    (rationalize 30.4)})

(push-measurement! {:temperature 82
                    :humidity    70
                    :pressure    (rationalize 29.2)})

(push-measurement! {:temperature 78
                    :humidity    90
                    :pressure    (rationalize 29.2)})
