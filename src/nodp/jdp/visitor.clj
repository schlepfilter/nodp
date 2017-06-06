(ns nodp.jdp.visitor
  (:require [aid.core :as aid]
            [cats.monad.maybe :as maybe]))

(def default-hierarchy
  [:commander
   [:sergeant
    :soldier
    :soldier
    :soldier]
   [:sergeant
    :soldier
    :soldier
    :soldier]])

(def target?
  (aid/build =
             :target
             :object))

(defmulti greet (aid/build and
                           target?
                           :target))

(defmethod greet :commander
  [_]
  (maybe/just "Good to see you commander"))

(defmethod greet :sergeant
  [_]
  (maybe/just "Hello sergeant"))

(defmethod greet :soldier
  [_]
  (maybe/just "Greetings soldier"))

(defmethod greet false
  [_]
  aid/nothing)

(aid/defcurried make-visit
                [hierarchy target]
                (maybe/map-maybe (comp greet
                                        (fn [object]
                                          {:target target
                                           :object object}))
                                  (flatten hierarchy)))

(def visit
  (make-visit default-hierarchy))

(visit :soldier)

(visit :sergeant)

(visit :commander)
