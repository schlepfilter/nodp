(ns nodp.helpers.window
  (:require [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp :include-macros true]
            [com.rpl.specter :as s]
            [nodp.helpers.primitives.event :as event]))

(declare inner-height)

(defn add-remove-listener
  [event-type listener]
  (js/addEventListener event-type listener)
  (swap! event/network-state
         (partial s/setval*
                  :cancel
                  (fn [_]
                    (js/removeEventListener event-type listener)))))

(frp/register
  (def resize
    (frp/event))

  (def popstate
    (frp/event))

  (def inner-height
    (frp/stepper js/window.innerHeight
                 (helpers/<$> :inner-height resize)))

  ;TODO define a macro to define behaviors and add and remove event listeners
  (add-remove-listener "resize" #(resize {:inner-height js/window.innerHeight}))

  (add-remove-listener
    "popstate" #(popstate {:location {:pathname js/location.pathname}})))
