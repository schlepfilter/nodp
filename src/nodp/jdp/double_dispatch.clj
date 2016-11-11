(ns nodp.jdp.double-dispatch
  (:require [clojure.math.combinatorics :as combo]
            [thi.ng.geom.core.intersect :as intersect]
            [thi.ng.geom.rect :as rect]))

(def asteroid
  {:flaming  true
   :position (rect/rect [0 0] [5 5])
   :subject  :teroid})

(def mir
  {:flaming  false
   :position (rect/rect [1 1] [2 2])
   :subject  :station})

(def meteroid
  {:flaming  false
   :position (rect/rect [10 10] [15 15])
   :subject  :teroid})

(def iss
  {:flmaing  false
   :position (rect/rect [12 12] [14 14])
   :subject  :station})

(def bodies
  #{asteroid mir meteroid iss})

(def intersect?
  (comp (partial apply intersect/intersect-rect-rect?)
        (partial map :position)))

;TODO implement collide with predicate dispatch when core.match supports predicate dispatch

;Road Map
;
;A good chunk of Maranget's algorithm for pattern matching has been implemented. We would like to flesh out the pattern matching functionality. Once that work is done, we'll move on to predicate dispatch.
;https://github.com/clojure/core.match/wiki/Overview

(filter intersect? (combo/combinations bodies 2))
