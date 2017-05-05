(ns nodp.helpers.location
  (:require [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp :include-macros true]))

(declare pathname)

(frp/register
  ;TODO define a macro to define behaviors and add event listeners
  (def popstate
    (frp/event))

  (def pathname
    (frp/stepper js/location.pathname
                 (helpers/<$> :pathname popstate)))

  (js/addEventListener "popstate"
                       (fn []
                         (popstate {:pathname js/location.pathname})))
  ;TODO add a function that calls removeEventListener to :cancel
  )
