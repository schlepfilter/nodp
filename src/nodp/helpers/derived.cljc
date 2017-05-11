(ns nodp.helpers.derived
  (:require [cats.context :as ctx]
            [com.rpl.specter :as s]
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

(defn if-not-then-else
  [if-function then-function else]
  (helpers/if-then-else (complement if-function)
                        then-function
                        else))

(def behaviorize
  (partial if-not-then-else
           behavior?
           behavior))

(defn xor
  ;TODO support variadic arguments
  [p q]
  (or (and p (not q))
      (and (not p) q)))

(helpers/defcurried eventize
                    [e a]
                    ;TODO refactor
                    (if (event? a)
                      a
                      (helpers/<$> (constantly a)
                                   e)))

(def has-event?
  (partial some event?))

(defn entitize
  [arguments]
  (map (if (has-event? arguments)
         (->> arguments
              (filter event?)
              first
              eventize)
         behaviorize)
       arguments))

(def has-argument?
  (helpers/build and
                 seq?
                 (comp (partial not= 1)
                       count)))

#?(:clj
   (do (defmacro transparent*
         [[f & more]]
         `(let [arguments# [~@more]]
            (if (xor (has-event? arguments#)
                     (some behavior? arguments#))
              (apply (if (has-event? arguments#)
                       (partial combine ~f)
                       (helpers/lift-a ~(count more) ~f))
                     (entitize arguments#))
              (apply ~f arguments#))))

       (defmacro transparent
         [expr]
         (walk/postwalk (fn [x]
                          (if (has-argument? x)
                            `(transparent* ~x)
                            x))
                        (macroexpand expr)))))

(defn buffer
  [size start e]
  (->> e
       (core/reduce (fn [accumulation element]
                      (s/setval s/END
                                [element]
                                (helpers/if-then-else (comp (partial =
                                                                     size)
                                                            count)
                                                      rest
                                                      accumulation)))
                    (rest []))
       (combine vector (core/count e))
       (core/filter (fn [[n xs]]
                      (and (= (count xs) size)
                           (= (rem (- n size) start) 0))))
       (helpers/<$> second)))

(def mean
  (helpers/build (partial combine /)
                 core/+
                 core/count))

(def switcher
  (comp helpers/join
        behavior/stepper))
