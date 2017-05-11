(ns nodp.helpers.primitives.behavior
  (:refer-clojure :exclude [stepper time])
  (:require [cats.builtin]
            [cats.protocols :as protocols]
            [cats.util :as util]
            [com.rpl.specter :as s]
            [nodp.helpers :as helpers]
            [nodp.helpers.primitives.event :as event]
            [nodp.helpers.tuple :as tuple])
  #?(:clj
     (:import [clojure.lang IDeref])))

(declare context)

(defrecord Behavior
  [id]
  protocols/Contextual
  (-get-context [_]
    context)
  IDeref
  (#?(:clj  deref
      :cljs -deref) [_]
    ((helpers/<*> (comp id
                        :function)
                  :time)
      @event/network-state))
  protocols/Printable
  (-repr [_]
    (str "#[behavior " id "]")))

(util/make-printable Behavior)

(defn behavior**
  [id f]
  (swap! event/network-state (partial s/setval* [:function id] f))
  (Behavior. id))

(defn behavior*
  [f]
  (-> @event/network-state
      event/get-id
      (behavior** f)))

(defn get-function
  [b network]
  ((:id b) (:function network)))

(defn get-value
  [b t network]
  ((get-function b network) t))

(def context
  (helpers/reify-monad
    (comp behavior*
          constantly)
    (fn [ma f]
      (behavior* (fn [t]
                   (-> (get-value ma t @event/network-state)
                       f
                       (get-value t @event/network-state)))))))

(def registry
  (atom []))

(def register*
  (comp (partial swap! registry)
        ;TODO fix m/curry
        ;((m/curry s/setval*) s/END)
        ; ^--- The given function doesn't have arity metadata, provide an arity for currying.
        ((helpers/curry 3 s/setval*) s/END)
        vector))

#?(:clj (defmacro register
          [body]
          `(register* (fn []
                        ~@body))))

(defn start
  ([]
   (start #?(:clj  Double/POSITIVE_INFINITY
             :cljs js/Number.POSITIVE_INFINITY)))
  ([rate]
   (reset! event/network-state (event/get-initial-network))
   (def time
     (behavior* identity))
   (run! helpers/funcall @registry)))

(def restart
  ;TODO call stop
  start)

(defn get-middle
  [left right]
  (+ left (quot (- right left) 2)))

(defn first-pred-index
  [pred left right coll]
  (if (= left right)
    left
    (if (->> (get-middle left right)
             (get coll)
             pred)
      (recur pred left (get-middle left right) coll)
      (recur pred (inc (get-middle left right)) right coll))))

(defn last-pred
  [default pred coll]
  (nth coll
       (dec (first-pred-index (complement pred) 0 (count coll) coll))
       default))

(defn get-stepper-value
  [a e t network]
  (->> network
       (event/get-occs (:id e))
       (last-pred (event/get-unit a) (comp (partial > @t)
                                           deref
                                           tuple/fst))
       tuple/snd))

(defn stepper
  [a e]
  (behavior* (fn [t]
               (get-stepper-value a e t @event/network-state))))

(defn get-time-transform-function
  ;TODO refactor
  [any-behavior time-behavior network]
  (comp (get-function any-behavior network)
        (get-function time-behavior network)))

(defn time-transform
  ;TODO refactor
  [any-behavior time-behavior]
  (behavior* (get-time-transform-function any-behavior
                                          time-behavior
                                          @event/network-state)))

;TODO implement calculus after a Clojure/ClojureScript library for symbolic computation is released
