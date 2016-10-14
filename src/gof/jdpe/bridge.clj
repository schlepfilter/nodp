(ns gof.jdpe.bridge
  (:require [gof.helpers :as helpers]))

(defn- start
  [engine]
  (-> engine
      (update :running (constantly true))
      (update :action conj "Engine started")))

(defn- add-power-action
  [engine]
  (->> (:power engine)
       (str "Engine power increased to ")
       (update engine :action conj)))

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

(def turn-on
  start)

(def accelerate
  increase-power)

(def break
  decrease-power)

(def engine
  {:action  []
   :power   0
   :running false})

((comp break accelerate turn-on) engine)
