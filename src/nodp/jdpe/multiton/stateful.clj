(ns nodp.jdpe.multiton
  (:require [nodp.helpers :as helpers]))

(defn- get-generator
  []
  (atom 0))

(def generator
  {:engine  (get-generator)
   :vehicle (get-generator)})

(def get-set-next-serial!
  (comp (partial (helpers/flip swap!) inc)
        generator))

(defn- label
  [k]
  (str "next " (name k) ":"))

(def print-next-serial
  (helpers/build println
                 label
                 get-set-next-serial!))

(dotimes [_ 3]
  (print-next-serial :engine))

(dotimes [_ 3]
  (print-next-serial :vehicle))

