(ns nodp.jdpe.strategy)

(defn make-change-speed
  [gear]
  (fn [speed]
    (str "Working out correct gear at "
         speed
         "mph for a "
         (-> gear
             name
             .toUpperCase)
         " gearbox")))

(def change-standard-speed
  (make-change-speed :standard))

(map change-standard-speed [20 40])

(def change-sport-speed
  (make-change-speed :sport))

(map change-sport-speed [20 40])
