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

  (js/addEventListener "resize"
                       (fn []
                         (resize {:inner-height js/window.innerHeight})))

  ;TODO add a function that calls removeEventListener to :cancel

  (js/addEventListener "popstate"
                       (fn []
                         ;TODO namespace pathname
                         (popstate {:pathname js/location.pathname}))))
