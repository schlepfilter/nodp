(ns nodp.helpers.derived.behavior
  (:refer-clojure :exclude [stepper])
  (:require [cats.context :as ctx]
            [cats.monad.maybe :as maybe]
            [nodp.helpers.time :as time]
            [nodp.helpers.primitives.behavior :as behavior]))

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
                          lower-limit
                          integration]
                       (cond (<= @lower-limit @past-time)
                             (maybe/just (+ (* (+ current-latest past-latest)
                                               (- @current-time @past-time))
                                            (maybe/maybe 0
                                                         integration
                                                         identity)))
                             ;TODO handle the case in which (< @lower-limit @current-time)
                             :else integration))
                     t
                     b))

(defn derivative
  [b]
  (behavior/calculus (fn [current-latest past-latest current-time past-time & _]
                       (maybe/just (/ (- current-latest past-latest)
                                      (- @current-time @past-time))))
                     (time/time 0)
                     b))
