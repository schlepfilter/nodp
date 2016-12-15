(ns nodp.hfdp.observer
  (:require [clojure.string :as str]
            [beicon.core :as rx]
            [incanter.distributions :as distributions]
            [nodp.helpers :as helpers]))

(def subject (rx/subject))

(def pressure-stream
  (rx/map :pressure subject))

(def delta-stream
  (->> pressure-stream
       (rx/buffer 2 1)
       (rx/map (partial apply -))))

(defmacro casep
  ([x pred expr]
   `(if (or (= ~pred :else) (~pred ~x))
      ~expr))
  ([x pred expr & clauses]
   `(if (~pred ~x)
      ~expr
      (casep ~x ~@clauses))))

(defn- forecast
  [delta]
  (casep delta
         pos? "Improving weather on the way!"
         zero? "More of the same"
         :else "Watch out for cooler, rainy weather"))

(def forecast-stream
  (rx/map forecast delta-stream))

(rx/subscribe forecast-stream println)

(def temprature-stream
  (rx/map :temprature subject))

(def scan-temprature
  (partial (helpers/flip rx/scan) temprature-stream))

(def max-stream
  (scan-temprature max))

(def min-stream
  (scan-temprature min))

(def mean-stream
  (scan-temprature (comp distributions/mean
                         vector)))

(def statistic-stream
  (->> (rx/zip mean-stream max-stream min-stream)
       (rx/map (comp str/join
                     (partial interleave
                              ["Avg/Max/Min temperature = " "/" "/"])))))

(rx/subscribe statistic-stream println)

(rx/push! subject {:temprature 80
                   :humidity   65
                   :pressure   (rationalize 30.4)})

(rx/push! subject {:temprature 82
                   :humidity   70
                   :pressure   (rationalize 29.2)})

(rx/push! subject {:temprature 78
                   :humidity   90
                   :pressure   (rationalize 29.2)})
