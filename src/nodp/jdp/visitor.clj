(ns nodp.jdp.visitor
  (:require [cats.monad.maybe :as maybe]
            [nodp.helpers :as helpers]))

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
  (helpers/build =
                 :target
                 :object))

(defmulti greet (helpers/build and
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
  (maybe/nothing))

(defmacro defcurried
  [f-name bindings body]
  `(def ~f-name
     (->> (fn ~bindings
            ~body)
          (helpers/curry ~(count bindings)))))

(defcurried make-visit
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
