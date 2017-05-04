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
