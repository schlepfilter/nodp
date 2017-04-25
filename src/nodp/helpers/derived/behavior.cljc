(ns nodp.helpers.derived.behavior
  (:refer-clojure :exclude [stepper])
  (:require [cats.context :as ctx]
            [cats.monad.maybe :as maybe]
            [nodp.helpers.primitives.behavior :as behavior]
            [nodp.helpers.primitives.event :as event]
            [nodp.helpers :as helpers]))

(defn behavior
  [a]
  (->> a
       nodp.helpers/pure
       (ctx/with-context behavior/context)))

(defn make-entity?
  [entity-type]
  (comp (partial = entity-type)
        type))

(def event?
  (make-entity? nodp.helpers.primitives.event.Event))

(def behavior?
  (make-entity? nodp.helpers.primitives.behavior.Behavior))

(helpers/defcurried entitize
                    [pred entity-unit x]
                    (if (pred x)
                      x
                      (entity-unit x)))

(def eventize
  (entitize event? (partial nodp.helpers/pure event/context)))

(def behaviorize
  (entitize behavior? behavior))

(defn xor
  ;TODO support variadic arguments
  [p q]
  (or (and p (not q))
      (and (not p) q)))

(def xnor
  (complement xor))

#?(:clj
   (defmacro lifting
     [[f & more]]
     `(let [arguments# [~@more]]
        (if (xnor (some event? arguments#)
                  (some behavior? arguments#))
          (apply ~f arguments#)
          (apply (helpers/lift-a ~(count more) ~f)
                 (map (if (some event? arguments#)
                        eventize
                        behaviorize)
                      arguments#))))))

(defn stepper
  [a e]
  (behavior/switcher (behavior a)
                     (nodp.helpers/<$> behavior e)))

(defn get-delta-number
  [current-time past-time]
  (- @current-time @past-time))

(defn integral
  [k t b]
  (behavior/calculus
    (fn [current-latest past-latest current-time past-time integration]
      (maybe/just (+ (case k
                       :left (* past-latest
                                (get-delta-number current-time past-time))
                       :right (* current-latest
                                 (get-delta-number current-time past-time))
                       :trapezoid (/ (* (+ current-latest past-latest)
                                        (get-delta-number current-time
                                                          past-time))
                                     2))
                     (maybe/maybe 0
                                  integration
                                  identity))))
    (maybe/just t)
    b))

(defn derivative
  [b]
  (behavior/calculus (fn [current-latest past-latest current-time past-time _]
                       (maybe/just (/ (- current-latest past-latest)
                                      (- @current-time @past-time))))
                     helpers/nothing
                     b))
