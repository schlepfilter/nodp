(ns nodp.jdpe.multiton.core
  (:require [aid.core :as aid]))

(def make-describe
  ((aid/flip ((aid/curry 4 str) "next ")) ": "))

(map (make-describe "engine") (range 3))

(map (make-describe "vehicle") (range 3))
