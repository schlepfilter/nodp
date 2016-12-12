(ns nodp.jdpe.multiton
  (:require [nodp.helpers :as helpers]))

(defn- get-generator
  []
  (atom 0))

(def generator
  {:engine  (get-generator)
   :vehicle (get-generator)})

(defn- get-next-serial
  [k]
  (-> k
      generator
      (swap! inc)))

;This definition is less readable.
;(def get-next-serial
;  (comp (partial (helpers/flip swap!) inc)
;        generator))

(defn- get-description
  [k]
  (str "next " (name k) ":"))

(def print-next-serial
  (helpers/build println
                 get-description
                 get-next-serial))

(dotimes [_ 3]
  (print-next-serial :engine))

(dotimes [_ 3]
  (print-next-serial :vehicle))
