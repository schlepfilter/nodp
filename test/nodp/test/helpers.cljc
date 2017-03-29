(ns nodp.test.helpers
  (:require [clojure.test.check.generators :as gen]
            [nodp.helpers.unit :as unit]))

(def function
  (gen/fmap (fn [_]
              (memoize (fn [_]
                         (gen/generate gen/any))))
            (gen/return unit/unit)))

