(ns nodp.hfdp.observer
  (:require [clojure.string :as str]
            [beicon.core :as rx]
            [incanter.distributions :as distributions]
            [nodp.helpers :as helpers]))

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

(def scan-temperature
  (partial (helpers/flip rx/scan) temperature-stream))

(def max-stream
  (scan-temperature max))

(def min-stream
  (scan-temperature min))

(def mean-stream
  (scan-temperature (comp distributions/mean
                          vector)))

(def statistic-stream
  (->> (rx/zip mean-stream max-stream min-stream)
       (rx/map (comp str/join
                     (partial interleave
                              ["Avg/Max/Min temperature = " "/" "/"])))))

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
