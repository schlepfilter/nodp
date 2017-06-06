(ns nodp.jdpe.multiton.cell
  (:require [aid.core :as aid]
            [com.rpl.specter :as s]))

(def generator
  (atom {:engine  0
         :vehicle 0}))

(def get-set-next-serial!
  (comp (partial swap! generator)
        ((aid/flip (aid/curry s/transform*)) inc)))

(def label
  (comp ((aid/flip ((aid/curry 3 str) "next ")) ":")
        name))

(def print-next-serial
  (aid/build println
             label
             get-set-next-serial!))

(dotimes [_ 3]
  (print-next-serial :engine))

(dotimes [_ 3]
  (print-next-serial :vehicle))
