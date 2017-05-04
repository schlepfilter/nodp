(ns nodp.helpers.primitives.behavior
  (:require [cats.protocols :as protocols]
            [com.rpl.specter :as s]
            [nodp.helpers :as helpers]
            [nodp.helpers.primitives.event :as event])
  #?(:clj
     (:import [clojure.lang IDeref])))

(declare context)

(defn get-function
  [b network]
  ((:id b) (:function network)))

(defrecord Behavior
  [id]
  protocols/Contextual
  (-get-context [_]
    context)
  IDeref
  (#?(:clj  deref
      :cljs -deref) [b]
    ((get-function b @helpers/network-state)
      (:time @helpers/network-state))))

(defn behavior**
  [id & fs]
  (swap! helpers/network-state
         (partial helpers/call-functions
                  (map (fn [f]
                         (f id))
                       fs)))
  (Behavior. id))

(defn behavior*
  [f]
  (behavior** (event/get-id @helpers/network-state)
              (fn [id]
                (partial s/setval* [:function id] f))))

(def context
  (helpers/reify-monad
    (fn [a]
      (behavior* (constantly a)))
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
