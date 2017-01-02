(ns nodp.jdpe.multiton.core)

(defn- make-describe
  [generator]
  (partial str "next " generator ": "))

(map (make-describe "engine") (range 3))

(map (make-describe "vehicle") (range 3))
