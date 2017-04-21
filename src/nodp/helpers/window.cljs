(ns nodp.helpers.window
  (:require [nodp.helpers.primitives.behavior :as behavior]))

(declare inner-height)

(declare inner-width)

(behavior/set-registry! "window" inner-height inner-width)
