(ns nodp.jdp.double-dispatch
  (:require [aid.core :as aid]
            [clojure.math.combinatorics :as combo]
            [thi.ng.geom.core.intersect :as intersect]
            [thi.ng.geom.rect :as rect]))

(def position-m
  {:flaming-asteroid  (rect/rect [0 0] [5 5])
   :space-station-iss (rect/rect [12 12] [14 14])
   :space-station-mir (rect/rect [1 1] [2 2])
   :meteroid          (rect/rect [10 10] [15 15])})

(def get-collisions
  (comp (partial mapcat (comp combo/permutations
                              (partial map first)))
        (partial filter (comp (partial apply intersect/intersect-rect-rect?)
                              (partial map last)))
        (partial (aid/flip combo/combinations) 2)))

(get-collisions position-m)

;TODO implement get-action with predicate dispatch when core.match supports predicate dispatch

;Road Map
;
;A good chunk of Maranget's algorithm for pattern matching has been implemented. We would like to flesh out the pattern matching functionality. Once that work is done, we'll move on to predicate dispatch.
;https://github.com/clojure/core.match/wiki/Overview
