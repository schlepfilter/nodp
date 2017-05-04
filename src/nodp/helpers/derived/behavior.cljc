(ns nodp.helpers.derived.behavior
  (:require [cats.context :as ctx]
            [nodp.helpers.primitives.behavior :as behavior]))

(defn behavior
  [a]
  (->> a
       nodp.helpers/pure
       (ctx/with-context behavior/context)))

