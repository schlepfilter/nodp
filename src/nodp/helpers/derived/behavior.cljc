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

(def event?
  ;TODO refactor
  (comp (partial = nodp.helpers.primitives.event.Event)
        type))

(defn eventize
  ;TODO refactor
  [x]
  (if (event? x)
    x
    (nodp.helpers/pure event/context x)))

(def behavior?
  ;TODO refactor
  (comp (partial = nodp.helpers.primitives.behavior.Behavior)
        type))

(defn behaviorize
  ;TODO refactor
  [x]
  (if (behavior? x)
    x
    (behavior x)))

#?(:clj (defmacro lifting
          [[f & more]]
          ;TODO handle cases in which more contains constants
          ;TODO handle cases in which some of the arguments is an event
          ;TODO refactor
          `(let [arguments# [~@more]]
             (if (some event? arguments#)
               (if (some behavior? arguments#)
                 (apply ~f arguments#)
                 (apply (helpers/lift-a ~(count more) ~f)
                        (map eventize arguments#)))
               (if (some behavior? arguments#)
                 (apply (helpers/lift-a ~(count more) ~f)
                        (map behaviorize arguments#))
                 (apply ~f arguments#))))))

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
