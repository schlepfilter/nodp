(ns nodp.jdpe.multiton.cell
  (:require [com.rpl.specter :as s]
            [help]))

(def generator
  (atom {:engine  0
         :vehicle 0}))

(def get-set-next-serial!
  (comp (partial swap! generator)
        ((help/flip (help/curry s/transform*)) inc)))

(def label
  (comp ((help/flip ((help/curry 3 str) "next ")) ":")
        name))

(def print-next-serial
  (help/build println
              label
              get-set-next-serial!))

(dotimes [_ 3]
  (print-next-serial :engine))

(dotimes [_ 3]
  (print-next-serial :vehicle))
