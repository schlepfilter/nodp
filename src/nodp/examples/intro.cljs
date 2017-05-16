(ns ^:figwheel-always nodp.examples.intro
  (:require [nodp.helpers.frp :as frp]))

(def intro
  (frp/behavior [:div]))
