(ns nodp.helpers.primitives.behavior
  (:require [cats.protocols :as protocols]
            [com.rpl.specter :as s]
            [nodp.helpers :as helpers]
            [nodp.helpers.primitives.event :as event]))

(declare context)

(defrecord Behavior
  [id]
  protocols/Contextual
  (-get-context [_]
    context))

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
