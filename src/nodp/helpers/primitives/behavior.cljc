(ns nodp.helpers.primitives.behavior
  (:require [nodp.helpers :as helpers]))

(defn start
  ([]
   (start {}))
  ;TODO specify default sample-rate
  ([{:keys [sample-rate]}]
   (reset! helpers/network-state (helpers/get-initial-network))))

(def restart
  ;TODO call stop
  start)
