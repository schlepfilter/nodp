(ns gof.jdpe.bridge
  (:require [gof.helpers :as helpers]))

(def verb
  {true  "started"
   false "stopped"})

(def get-sentence
  (comp (partial str "Engine ")
        verb))

(defn- make-change-running
  [running]
  (fn [engine]
    (-> engine
        (assoc :running running)
        (update :actions conj (get-sentence running)))))

(def start
  (make-change-running true))

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

(def make-changeable?
  (partial every-pred :running))

(def increasable?
  (make-changeable? max-speed?))

(def min-speed?
  (comp (partial < 0) :power))

(def decreasable?
  (make-changeable? min-speed?))

(def increase-power
  (make-change-power increasable? inc))

(def decrease-power
  (make-change-power decreasable? dec))

(def stop
  (make-change-running false))

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
  (-> ((apply comp actions) engine)
      print-actions))

(run-actions {:engine  engine
              :actions [turn-off break accelerate turn-on]})

(run-actions {:engine  engine
              :actions [turn-off break accelerate accelerate-hard turn-on]})
