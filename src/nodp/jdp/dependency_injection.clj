(ns nodp.jdp.dependency-injection
  (:require [help.core :as help]))

(def make-smoke
  ((help/flip (help/curry 3 str)) " smoking "))

(def advanced-wizard-smoke
  (make-smoke "AdvancedWizard"))

(advanced-wizard-smoke "SecondBreakfastTobacco")