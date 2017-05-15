(ns nodp.helpers.window
  (:require [com.rpl.specter :as s]
            [nodp.helpers :as helpers]
            [nodp.helpers.io :as io]
            [nodp.helpers.primitives.behavior
             :as behavior
             :include-macros true]
            [nodp.helpers.primitives.event :as event]))

(def popstate
  (event/->Event ::popstate))

(def resize
  (event/->Event ::resize))

(def inner-height
  (behavior/->Behavior ::inner-height))

(defn add-remove-listener
  [event-type listener]
  (js/addEventListener event-type listener)
  (swap! event/network-state
         (partial s/setval*
                  :cancel
                  (fn [_]
                    (js/removeEventListener event-type listener)))))

(behavior/register
  (io/redef-events [popstate resize])

  (behavior/redef inner-height
                  (->> resize
                       (helpers/<$> :inner-height)
                       (behavior/stepper js/innerHeight)))

  ;TODO define a macro to define behaviors and add and remove event listeners
  (add-remove-listener
    "popstate"
    #(popstate {:location {:pathname js/location.pathname}}))

  (add-remove-listener "resize"
                       #(resize {:inner-height js/innerHeight})))
