(ns nodp.helpers.primitives.behavior
  (:refer-clojure :exclude [time])
  (:require [cats.protocols :as protocols]
            [cats.util :as util]
            [com.rpl.specter :as s]
            [nodp.helpers :as helpers]
            [nodp.helpers.primitives.event :as event]
            [nodp.helpers.time :as time]
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
    ((id (:function @event/network-state))
      (:time @event/network-state)))
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

(def context
  (helpers/reify-monad
    (comp behavior*
          constantly)
    ;TODO implement >>=
    (fn [])))

;TODO make registration public
(def registry
  (atom []))

(defn register*
  ;TODO refactor
  [f]
  (swap! registry (partial s/setval* s/END [f])))

#?(:clj (defmacro register
          [body]
          `(register* (fn []
                        ~@body))))

(defn start
  ([]
   (start {}))
  ;TODO specify default sample-rate
  ([{:keys [sample-rate]}]
   (reset! event/network-state (event/get-initial-network))
   (def time
     (behavior* identity))
   (run! helpers/funcall @registry)))

(def restart
  ;TODO call stop
  start)

(defn get-function
  [b network]
  ((:id b) (:function network)))

(defn get-middle
  [left right]
  (+ left (quot (- right left) 2)))

(defn first-pred-index
  [pred left right coll]
  (if (= left right)
    left
    (if (pred (get coll (get-middle left right)))
      (recur pred left (get-middle left right) coll)
      (recur pred (inc (get-middle left right)) right coll))))

(defn last-pred
  [default pred coll]
  (nth coll
       (dec (first-pred-index (complement pred) 0 (count coll) coll))
       default))

(defn get-switcher-value
  [b e t network]
  ((get-function (->> network
                      (event/get-occs (:id e))
                      (last-pred (tuple/tuple (time/time 0) b)
                                 (comp (partial > @t)
                                       deref
                                       tuple/fst))
                      tuple/snd)
                 network)
    t))

(defn switcher
  [b e]
  (behavior* (fn [t]
               (get-switcher-value b e t @event/network-state))))

;TODO implement calculus after a Clojure/ClojureScript library for symbolic computation is released
