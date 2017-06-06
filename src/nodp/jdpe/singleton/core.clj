(ns nodp.jdpe.singleton.core)

(def describe
  (partial str "next serial: "))

(map describe (range 3))
