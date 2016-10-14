(ns gof.jdpe.bridge
  (:require [gof.helpers :as helpers]))

(defn- start
  [engine]
  (-> engine
      (assoc :running true)
      (update :actions conj "Engine started")))

(defn- add-power-action
  [engine]
  (->> (:power engine)
       (str "Engine power increased to ")
       (update engine :actions conj)))

(defn- unconditionally-change-power
  [engine change]
  (-> engine
      (update :power change)
      add-power-action))

(defn- make-change-power
  [conditional change]
  (fn [engine]
    (if (conditional engine)
      (unconditionally-change-power engine change)
      engine)))

(def max-speed?
  (comp (partial > 10) :power))

(def increasable?
  (helpers/build and :running max-speed?))

(def min-speed?
  (comp (partial < 0) :power))

(def decreasable?
  (helpers/build and :running min-speed?))

(def increase-power
  (make-change-power increasable? inc))

(def decrease-power
  (make-change-power decreasable? dec))

(defn- stop
  [engine]
  (-> engine
      (assoc :running false)
      (update :actions conj "Engine stopped")))

(def turn-on
  start)

(def accelerate
  increase-power)

(def accelerate-hard
  (comp accelerate accelerate))

(def break
  decrease-power)

(def turn-off
  stop)

(def engine
  {:actions []
   :power   0
   :running false})

(def print-actions
  (comp helpers/printall :actions))

(defn- run-actions
  [{:keys [engine actions]}]
  (print-actions ((apply comp actions) engine)))

(run-actions {:engine engine
              :actions [turn-off break accelerate turn-on]})

(run-actions {:engine engine
              :actions [turn-off break accelerate accelerate-hard turn-on]})
