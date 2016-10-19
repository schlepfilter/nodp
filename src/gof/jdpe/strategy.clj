(ns gof.jdpe.strategy)

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

(change-standard-speed 20)
(change-standard-speed 40)

(def change-sport-speed
  (make-change-speed :sport))

(change-sport-speed 20)
(change-sport-speed 40)
