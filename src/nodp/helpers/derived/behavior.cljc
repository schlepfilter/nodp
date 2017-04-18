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

(defn stepper
  [a e]
  (behavior/switcher (behavior a)
                     (nodp.helpers/<$> behavior e)))

(defn integral
  [t b]
  (behavior/calculus (fn [current-latest
                          past-latest
                          current-time
                          past-time
                          lower-limit-maybe
                          integration]
                       (cond (<= @@lower-limit-maybe @past-time)
                             (maybe/just (+ (* (+ current-latest past-latest)
                                               (- @current-time @past-time))
                                            (maybe/maybe 0
                                                         integration
                                                         identity)))
                             ;TODO handle cases in which (< @lower-limit @current-time)
                             :else integration))
                     (maybe/just t)
                     b))

(defn derivative
  [b]
  (behavior/calculus (fn [current-latest past-latest current-time past-time & _]
                       (maybe/just (/ (- current-latest past-latest)
                                      (- @current-time @past-time))))
                     helpers/nothing
                     b))
