(ns nodp.helpers.window
  (:require [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp :include-macros true]
            [com.rpl.specter :as s]
            [nodp.helpers.primitives.event :as event]))

;TODO specify an id
(declare popstate)

(declare resize)

(def inner-height
  (frp/->Behavior ::inner-height))

(defn add-remove-listener
  [event-type listener]
  (js/addEventListener event-type listener)
  (swap! event/network-state
         (partial s/setval*
                  :cancel
                  (fn [_]
                    (js/removeEventListener event-type listener)))))

(frp/register
  ;TODO rename id
  (def popstate
    (frp/event))

  (def resize
    (frp/event))

  (frp/redef inner-height
             (frp/stepper js/window.innerHeight
                          (helpers/<$> :inner-height resize)))

  ;TODO define a macro to define behaviors and add and remove event listeners
  (add-remove-listener
    "popstate" #(popstate {:location {:pathname js/location.pathname}}))

  (add-remove-listener "resize"
                       #(resize {:inner-height js/window.innerHeight})))
