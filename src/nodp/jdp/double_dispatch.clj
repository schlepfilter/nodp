(ns nodp.jdp.double-dispatch
  (:require [clojure.math.combinatorics :as combo]
            [thi.ng.geom.core.intersect :as intersect]
            [thi.ng.geom.rect :as rect]))

(def asteroid
  {:name     "FlamingAsteroid"
   :position (rect/rect [0 0] [5 5])})

(def mir
  {:name     "SpaceStationMir"
   :position (rect/rect [1 1] [2 2])})

(def meteroid
  {:name     "Meteroid"
   :position (rect/rect [10 10] [15 15])})

(def iss
  {:name     "SpaceStationIss"
   :position (rect/rect [12 12] [14 14])})

(def bodies
  #{asteroid mir meteroid iss})

(def intersect?
  (comp
    (partial apply intersect/intersect-rect-rect?)
    (partial map :position)))

(filter intersect? (combo/combinations bodies 2))
