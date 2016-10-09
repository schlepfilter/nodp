(ns hfdp.decorator
  (:require [clojure.string :as str]))

(def base
  {:dark-roast  {:name  "Dark Roast Coffee"
                 :price 0.99}
   :espresso    {:name  "Espresso"
                 :price 1.99}
   :house-blend {:name  "House Blend Coffee"
                 :price 0.89}})

(def condiment
  {:mocha {:name  "Mocha"
           :price 0.2}
   :soy   {:name  "Soy"
           :price 0.15}
   :whip  {:name  "Whip"
           :price 0.1}})

;TODO handle the case where the types of first and second elements differ
(defmulti add (comp type first vector))

(defmethod add String
  [& ss]
  (str/join ", " ss))

(defmethod add Number
  [& ns]
  (apply + ns))

(defn- get-ingredients
  [base-key condiment-keys]
  (cons (base-key base) (map condiment condiment-keys)))

(defn get-coffee
  [base-key & condiment-keys]
  (->> (get-ingredients base-key condiment-keys)
       (apply merge-with add)))

(get-coffee :dark-roast :mocha :mocha :whip)
