(ns gof.jdpe.bridge
  (:require [gof.helpers :as helpers]))

(defn- start
  [engine]
  (-> engine
      (update :running (constantly true))
      (update :action (partial (helpers/flip conj) "Engine started"))))

(defn- increase-power
  [engine]
  (-> engine
      (update :power inc)
      (update :action (partial (helpers/flip conj) "Engine power increased to "))))

(def turn-on
  start)

(def accelerate
  increase-power)

(def get-engine
  (constantly {:action  []
               :power   0
               :running false}))

((comp accelerate turn-on get-engine))
