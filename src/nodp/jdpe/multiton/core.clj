(ns nodp.jdpe.multiton.core
  (:require [help]))

(def make-describe
  ((help/flip ((help/curry 4 str) "next ")) ": "))

(map (make-describe "engine") (range 3))

(map (make-describe "vehicle") (range 3))
