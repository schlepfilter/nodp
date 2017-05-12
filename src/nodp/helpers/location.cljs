(ns nodp.helpers.location
  (:require [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp :include-macros true]
            [nodp.helpers.history :as history]
            [nodp.helpers.window :as window]))

(declare pathname)

(frp/register
  ;TODO define a macro to define behaviors
  (def pathname
    (frp/stepper js/location.pathname
                 (helpers/<$> (comp :pathname
                                    :location)
                              (helpers/<> window/popstate history/pushstate)))))
