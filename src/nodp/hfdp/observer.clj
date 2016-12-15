(ns nodp.hfdp.observer
  (:require [clojure.string :as str]
            [beicon.core :as rx]
            [incanter.distributions :as distributions]
            [nodp.helpers :as helpers]))

(def subject (rx/subject))

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

(def get-statistics
  (comp str/join
        (partial interleave ["Avg/Max/Min temperature = " "/" "/"])))

(def statistics-stream
  (->> (rx/zip mean-stream max-stream min-stream)
       (rx/map get-statistics)))

(rx/subscribe statistics-stream println)

(rx/push! subject {:temprature 80
                   :humidity   65
                   :pressure   30.4})

(rx/push! subject {:temprature 82
                   :humidity   70
                   :pressure   29.2})

(rx/push! subject {:temprature 78
                   :humidity   90
                   :pressure   29.2})
