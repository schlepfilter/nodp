(ns nodp.helpers.time
  (:refer-clojure :exclude [time])
  (:require [cats.context :as ctx]
            [cats.core :as m]
            [cats.protocols :as p]
            [cats.util :as util]
    #?@(:clj  [
            [clj-time.coerce :as c]
            [clj-time.core :as t]]
        :cljs [[cljs-time.coerce :as c]
               [cljs-time.core :as t]]))
  #?(:clj
     (:import (clojure.lang IDeref))))

(defrecord Time
  [x]
  p/Contextual
  (-get-context [_]
    (reify
      p/Context
      p/Semigroup
      (-mappend [_ x* y*]
        (Time. (max @x* @y*)))
      p/Monoid
      (-mempty [_]
        (Time. 0))
      p/Functor
      (-fmap [_ f fa]
        (Time. (f @fa)))))
  IDeref
  (#?(:clj  deref
      :cljs -deref) [_]
    x)
  #?(:clj  Comparable
     :cljs IComparable)
  (#?(:clj  compareTo
      :cljs -compare) [x* y*]
    (compare @x* @y*))
  p/Printable
  (-repr [_]
    (str "#[time " x "]")))

(util/make-printable Time)

(def time
  ->Time)

(def epoch-state
  (atom 0))

(defn now-long
  []
  (-> (t/now)
      c/to-long))

(defn start
  []
  (->> (now-long)
       ;dec ensures times for events are strictly increasing.
       dec
       (reset! epoch-state)))

(defn now
  []
  (-> (now-long)
      (- @epoch-state)
      time))

;TODO remove this function after cats.context is fixed
(defn infer
  "Given an optional value infer its context. If context is already set, it
  is returned as is without any inference operation."
  {:no-doc true}
  ([]
   (when (nil? ctx/*context*)
     (ctx/throw-illegal-argument "No context is set."))
   ctx/*context*)
  ([v]
   (cond
     (satisfies? p/Contextual v)
     (p/-get-context v)
     :else
     (ctx/throw-illegal-argument
       (str "No context is set and it can not be automatically "
            "resolved from provided value")))))

;TODO remove this function after cats.context is fixed
(defn <$>
  [& more]
  (with-redefs [cats.context/infer infer]
    (apply m/<$> more)))

(defn to-real-time
  [t]
  (<$> (partial + @epoch-state)
       t))
