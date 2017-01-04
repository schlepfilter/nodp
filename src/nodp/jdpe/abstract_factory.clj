(ns nodp.jdpe.abstract-factory
  (:require [nodp.helpers :as helpers]))

(def description
  {:body    "Body shell parts"
   :chassis "Chassis parts"
   :window  "Window glassware"})

(defn make-get-parts
  [vehicle]
  (comp helpers/space-join
        (partial (helpers/flip concat) ["for a" (name vehicle)])
        vector
        description))

(def get-car-parts
  (make-get-parts :car))

(get-car-parts :body)
(get-car-parts :chassis)
(get-car-parts :window)
