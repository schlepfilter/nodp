(ns nodp.helpers.frp
  (:refer-clojure :exclude [stepper time])
  (:require [nodp.helpers.derived.behavior :as derived-behavior]
            [nodp.helpers.primitives.behavior :as behavior :include-macros true]
            [nodp.helpers.primitives.event :as event]
            [nodp.helpers.io :as io]))

#?(:clj (defmacro register
          [& body]
          `(behavior/register ~body)))

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

(def on
  io/on)

(def stepper
  behavior/stepper)

(def switcher
  derived-behavior/switcher)
