(ns nodp.jdpe.bridge
  (:require [nodp.helpers :as helpers]
            [com.rpl.specter :as s]))

(def verb
  {true  "started"
   false "stopped"})

(def get-sentence
  (comp (partial str "Engine ")
        verb))

(def make-change-running
  (helpers/build comp
                 ((helpers/curry s/setval*) :running)
                 (comp ((helpers/curry s/setval*) [:actions s/END])
                       vector
                       get-sentence)))

;This definition is less readable.
;(defn- make-change-running
;  [running]
;  (comp (partial s/setval* :running running)
;        (partial s/setval* [:actions s/END] (-> running
;                                                get-sentence
;                                                vector))))

(def start
  (make-change-running true))

(def add-power-action
  (helpers/build (partial s/setval* [:actions s/END])
                 (comp vector
                       (partial str "Engine power increased to ")
                       :power)
                 identity))

;This definition may be more readable.
;(defn- add-power-action
;  [engine]
;  (->> engine
;       :power
;       (str "Engine power increased to ")
;       (update engine :actions conj)))

(defn- unconditionally-change-power
  [{:keys [change engine]}]
  (->> engine
       (s/transform :power change)
       add-power-action))

(defn- make-change-power
  [conditional change]
  (fn [engine]
    (if (conditional engine)
      (unconditionally-change-power {:change change
                                     :engine engine})
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

(def default-engine
  {:actions []
   :power   0
   :running false})

(defn- get-actions
  [& commands]
  (-> ((apply comp commands) default-engine)
      :actions))

(get-actions turn-off break accelerate turn-on)

(get-actions turn-off break accelerate accelerate-hard turn-on)
