(ns nodp.jdp.dependency-injection
  (:require [aid.core :as aid]))

(def make-smoke
  ((aid/flip (aid/curry 3 str)) " smoking "))

(def advanced-wizard-smoke
  (make-smoke "AdvancedWizard"))

(advanced-wizard-smoke "SecondBreakfastTobacco")