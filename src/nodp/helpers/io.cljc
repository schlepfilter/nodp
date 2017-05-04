(ns nodp.helpers.io
  (:require [cats.context :as ctx]
            [nodp.helpers.primitives.event :as event]))

(defn event
  []
  (->> (nodp.helpers/mempty)
       (ctx/with-context event/context)))