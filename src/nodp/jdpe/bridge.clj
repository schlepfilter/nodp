(ns nodp.jdpe.bridge
  (:require [nodp.helpers :as helpers]
            [com.rpl.specter :as s]))

(def verb
  {true  "started"
   false "stopped"})

(def get-sentence
  (comp (partial str "Engine ")
        verb))

(defn- make-change-running
  [running]
  (comp (partial s/setval* :running running)
        (partial s/setval* [:actions s/END] (-> running
                                                get-sentence
                                                vector))))

;This definition is less readable.
;(def make-change-running
;  (comp (partial apply comp)
;        (juxt ((helpers/curry s/setval*) :running)
;              (comp ((helpers/curry s/setval*) [:actions s/END])
;                    vector
;                    get-sentence))))

(def start
  (make-change-running true))

(defn- add-power-action
  [engine]
  (s/setval [:actions s/END]
            (->> :power
                 engine
                 (str "Engine power increased to ")
                 vector)
            engine))

;This definition may be more readable.
;(defn- add-power-action
;  [engine]
;  (->> engine
;       :power
;       (str "Engine power increased to ")
;       (update engine :actions conj)))

(defn- unconditionally-change-power
  [engine change]
  (->> engine
      (s/transform :power change)
      add-power-action))

(defn- make-change-power
  [conditional change]
  (fn [engine]
    (if (conditional engine)
      (unconditionally-change-power engine change)
      engine)))

(def max-speed?
  (comp (partial > 10)
        :power))

(def make-changeable?
  (partial every-pred :running))

(def increasable?
  (make-changeable? max-speed?))

(def min-speed?
  (comp (partial < 0)
        :power))

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
  (comp accelerate
        accelerate))

(def break
  decrease-power)

(def turn-off
  stop)

(def engine
  {:actions []
   :power   0
   :running false})

(def print-actions
  (comp helpers/printall
        :actions))

(defn- run-actions
  [{:keys [engine actions]}]
  (-> ((apply comp actions) engine)
      print-actions))

(run-actions {:engine  engine
              :actions [turn-off break accelerate turn-on]})

(run-actions {:engine  engine
              :actions [turn-off break accelerate accelerate-hard turn-on]})
