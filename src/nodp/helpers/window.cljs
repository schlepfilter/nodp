(ns nodp.helpers.window
  (:require [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp :include-macros true]
            [com.rpl.specter :as s]
            [nodp.helpers.primitives.event :as event]))

(declare inner-height)

(frp/register
  ;TODO define a macro to define behaviors and add and remove event listeners
  (def resize
    (frp/event))

  (def popstate
    (frp/event))

  (def inner-height
    (frp/stepper js/window.innerHeight
                 (helpers/<$> :inner-height resize)))

  (letfn [(add-resize []
            (resize {:inner-height js/window.innerHeight}))]
    (js/addEventListener "resize" add-resize)
    (swap! event/network-state
           (partial s/setval*
                    :cancel
                    (fn [_]
                      (js/removeEventListener "resize" add-resize)))))


  (js/addEventListener
    "popstate"
    (fn []
      (popstate {:location {:pathname js/location.pathname}}))))
