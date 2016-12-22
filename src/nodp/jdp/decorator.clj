(ns nodp.jdp.decorator
  (:require [plumbing.map :as map]))

(def troll
  {:attack "The troll swings at you with a club!"
   :flee   "The troll shrieks in horror and runs away!"
   :kind   "simple"
   :power  10})

(def smart
  {:attack "It throws a rock at you!"
   :flee   "It calls for help!"
   :kind   "smart"
   :power  20})

(defmulti decorate (comp first
                         vector))

;TODO implement decorate with predicate dispatch when core.match supports predicate dispatch

;(map/merge-with-key decorate troll smart)
