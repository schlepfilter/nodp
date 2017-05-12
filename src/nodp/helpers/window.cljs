(ns nodp.helpers.window
  (:require [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp :include-macros true]))

(declare inner-height)

(frp/register
  ;TODO define a macro to define behaviors and add event listeners
  (def resize
    (frp/event))

  (def popstate
    (frp/event))

  (def inner-height
    (frp/stepper js/window.innerHeight
                 (helpers/<$> :inner-height resize)))

  ;TODO remove listeners on stop
  (js/addEventListener "resize"
                       (fn []
                         (resize {:inner-height js/window.innerHeight})))

  (js/addEventListener
    "popstate"
    (fn []
      (popstate {:location {:pathname js/location.pathname}}))))
