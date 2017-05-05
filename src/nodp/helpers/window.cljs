(ns nodp.helpers.window
  (:require [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp]))

(declare inner-height)

(frp/register (fn []
                (def resize
                  (frp/event))

                (def inner-height
                  (frp/stepper js/window.innerHeight
                               (helpers/<$> :inner-height resize)))))
