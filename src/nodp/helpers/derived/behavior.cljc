;event and behavior namespaces are separated to limit the impact of :refer-clojure :exclude for stepper
(ns nodp.helpers.derived.behavior
  (:require [cats.context :as ctx]
            [nodp.helpers :as helpers]
            [nodp.helpers.primitives.behavior :as behavior]))

(defn behavior
  [a]
  (->> a
       nodp.helpers/pure
       (ctx/with-context behavior/context)))

(def switcher
  (comp helpers/join
        behavior/stepper))
