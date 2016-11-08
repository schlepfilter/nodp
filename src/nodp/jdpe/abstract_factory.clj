(ns nodp.jdpe.abstract-factory)

(def description
  {:body    "Body shell parts"
   :chassis "Chassis parts"
   :window  "Window glassware"})

(defn make-get-parts
  [vehicle]
  (comp description
        (partial str " for a " (name vehicle))))

(def get-car-parts
  (make-get-parts :car))

(get-car-parts :body)
(get-car-parts :chassis)
(get-car-parts :window)
