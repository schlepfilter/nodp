(ns nodp.helpers.derived.behavior
  (:require [cats.context :as ctx]
            [cats.core :as m]
            [nodp.helpers.primitives.behavior :as behavior]))

(defn behavior
  [a]
  (->> a
       m/pure
       (ctx/with-context behavior/context)))