(ns nodp.helpers.frp
  (:refer-clojure :exclude [stepper time])
  (:require [nodp.helpers.derived.behavior :as derived-behavior]
            [nodp.helpers.primitives.behavior :as behavior :include-macros true]
            [nodp.helpers.primitives.event :as event]
            [nodp.helpers.io :as io]))

#?(:clj (defmacro register
          [& more]
          `(behavior/register ~more)))

(def restart
  (comp (fn [_]
          (def time
            behavior/time))
        behavior/restart))

(def activate
  event/activate)

(def event
  io/event)

(def behavior
  derived-behavior/behavior)

(declare time)

(def switcher
  behavior/switcher)

(def stepper
  derived-behavior/stepper)
