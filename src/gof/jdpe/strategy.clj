(ns gof.jdpe.strategy)

(defn make-change-speed
  [gear]
  (let [capitalized-gear (-> gear
                             name
                             .toUpperCase)]
    (fn [speed]
     (str "Working out correct gear at "
          speed
          "mph for a "
          capitalized-gear
          " gearbox"))))

(def change-standard-speed
  (make-change-speed :standard))

(change-standard-speed 20)
(change-standard-speed 40)

(def change-sport-speed
  (make-change-speed :sport))

(change-sport-speed 20)
(change-sport-speed 40)
