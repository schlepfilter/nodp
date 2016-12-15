(ns nodp.jdpe.multiton.stateless)

(defn- make-describe
  [generator]
  (partial str "next " generator ": "))

(map (make-describe "engine") (range 3))

(map (make-describe "vehicle") (range 3))
