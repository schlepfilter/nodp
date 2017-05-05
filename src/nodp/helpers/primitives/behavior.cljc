(ns nodp.helpers.primitives.behavior
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
    ((id (:function @helpers/network-state))
      (:time @helpers/network-state)))
  protocols/Printable
  (-repr [_]
    (str "#[behavior " id "]")))

(util/make-printable Behavior)

(defn behavior**
  [id & fs]
  (->> fs
       (map (partial (helpers/flip helpers/funcall) id))
       (partial helpers/call-functions)
       (swap! helpers/network-state))
  (Behavior. id))

(defn behavior*
  [f]
  (behavior** (event/get-id @helpers/network-state)
              (fn [id]
                (partial s/setval* [:function id] f))))

(def context
  (helpers/reify-monad
    (comp behavior*
          constantly)
    ;TODO implement >>=
    (fn [])))

(defn start
  ([]
   (start {}))
  ;TODO specify default sample-rate
  ([{:keys [sample-rate]}]
   (reset! helpers/network-state (helpers/get-initial-network))))

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

(defn switcher
  [b e]
  (behavior* (fn [t]
               ;TODO refactor
               ((get-function (->> @helpers/network-state
                                   (event/get-occs (:id e))
                                   (last-pred (tuple/tuple (time/time 0) b)
                                              (comp (partial > @t)
                                                    deref
                                                    tuple/fst))
                                   tuple/snd)
                              @helpers/network-state)
                 t))))
