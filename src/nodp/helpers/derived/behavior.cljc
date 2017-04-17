(ns nodp.helpers.derived.behavior
  (:refer-clojure :exclude [stepper])
  (:require [cats.context :as ctx]
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
  [t b])

(defn derivative
  [b])
