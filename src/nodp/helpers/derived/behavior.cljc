(ns nodp.helpers.derived.behavior
  (:refer-clojure :exclude [stepper])
  (:require [cats.context :as ctx]
            [cats.monad.maybe :as maybe]
            [nodp.helpers.primitives.behavior :as behavior]
            [nodp.helpers :as helpers]))

(defn behavior
  [a]
  (->> a
       nodp.helpers/pure
       (ctx/with-context behavior/context)))

#?(:clj (defmacro lifting
          [[f & more]]
          ;TODO handle cases in which more contains constants
          ;TODO handle cases in which f won't be lifted
          `((helpers/lift-a ~(count more) ~f) ~@more)))

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
