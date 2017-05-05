(ns nodp.helpers.location
  (:require [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp :include-macros true]
            [nodp.helpers.window :as window]))

(declare pathname)

(frp/register
  ;TODO define a macro to define behaviors and add event listeners
  (def pathname
    (frp/stepper js/location.pathname
                 (helpers/<$> :pathname window/popstate))))
