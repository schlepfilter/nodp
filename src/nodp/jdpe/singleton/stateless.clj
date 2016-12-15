(ns nodp.jdpe.singleton.stateless)

(def describe
  (partial str "next serial: "))

(map describe (range 3))
