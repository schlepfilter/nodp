(ns nodp.jdp.dependency-injection
  (:require [nodp.helpers :as helpers]))

(def make-smoke
  ((helpers/flip (helpers/curry 3 str)) " smoking "))

(def advanced-wizard-smoke
  (make-smoke "AdvancedWizard"))

(advanced-wizard-smoke "SecondBreakfastTobacco")