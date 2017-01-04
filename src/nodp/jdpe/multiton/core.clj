(ns nodp.jdpe.multiton.core
  (:require [nodp.helpers :as helpers]))

(def make-describe
  ((helpers/flip ((helpers/curry 4 str) "next ")) ": "))

(map (make-describe "engine") (range 3))

(map (make-describe "vehicle") (range 3))
