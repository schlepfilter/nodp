(ns nodp.helpers.derived
  (:require [cats.context :as ctx]
            [nodp.helpers :as helpers]
            [nodp.helpers.clojure.core :as core]
            [nodp.helpers.primitives.behavior :as behavior]
            [nodp.helpers.primitives.event :as event]
            [nodp.helpers.tuple :as tuple]
    #?(:clj
            [clojure.walk :as walk])))

(helpers/defcurried add-edges
                    [parents child network]
                    (helpers/call-functions (map ((helpers/flip event/add-edge)
                                                   child)
                                                 parents)
                                            network))

(defn get-occs-or-latests-coll
  [initial ids network]
  (map (partial (helpers/flip (event/make-get-occs-or-latests initial))
                network)
       ids))

(defn make-combine-occs-or-latests
  [f]
  (comp (helpers/build tuple/tuple
                       (comp tuple/fst first)
                       (comp (partial apply f)
                             (partial map tuple/snd)))
        vector))

(defn get-combined-occs
  [f parents initial network]
  (apply (partial map
                  (make-combine-occs-or-latests f))
         (get-occs-or-latests-coll initial
                                   parents
                                   network)))

(helpers/defcurried modify-combine
                    [f parents initial child network]
                    (event/set-occs (get-combined-occs f
                                                       parents
                                                       initial
                                                       network)
                                    child
                                    network))

(defn combine
  [f & parent-events]
  ((helpers/build (comp event/event*
                        cons)
                  add-edges
                  (comp event/make-set-modify-modify
                        (modify-combine f)))
    (map :id parent-events)))

(defn make-entity?
  [entity-type]
  (comp (partial = entity-type)
        type))

(def event?
  (make-entity? nodp.helpers.primitives.event.Event))

(def behavior?
  (make-entity? nodp.helpers.primitives.behavior.Behavior))

(defn behavior
  [a]
  (->> a
       helpers/pure
       (ctx/with-context behavior/context)))

(defn behaviorize
  [a]
  ;TODO refactor
  (if (behavior? a)
    a
    (behavior a)))

(defn xor
  ;TODO support variadic arguments
  [p q]
  (or (and p (not q))
      (and (not p) q)))

(def xnor
  (complement xor))

(helpers/defcurried eventize
                    [e a]
                    ;TODO refactor
                    (if (event? a)
                      a
                      (helpers/<$> (constantly a)
                                   e)))

(defn entitize
  [arguments]
  ;TODO refactor
  (map (if (some event? arguments)
         (eventize (first (filter event? arguments)))
         behaviorize)
       arguments))

#?(:clj
   (do (defmacro transparent*
         ;TODO refactor
         [[f & more]]
         `(let [arguments# [~@more]]
            (if (xnor (some event? arguments#)
                      (some behavior? arguments#))
              (apply ~f arguments#)
              (apply (if (some event? arguments#)
                       (partial combine ~f)
                       (helpers/lift-a ~(count more) ~f))
                     (entitize arguments#)))))

       (defmacro transparent
         [expr]
         (walk/postwalk (fn [x]
                          ;TODO refactor
                          (cond (not (seq? x))
                                x
                                (= 1 (count x))
                                x
                                :else
                                `(transparent* ~x)))
                        (macroexpand expr)))))

(def mean
  (helpers/build (partial combine /)
                 core/+
                 core/count))

(def switcher
  (comp helpers/join
        behavior/stepper))
