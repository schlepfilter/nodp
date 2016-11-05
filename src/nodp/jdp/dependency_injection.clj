(ns nodp.jdp.dependency-injection
  (:require [nodp.helpers :as helpers]
            [cats.core :as m]))

(def make-smoke
  ((m/curry 3 (helpers/flip str)) " smoking "))

(def advanced-wizard-smoke
  (make-smoke "AdvancedWizard"))

(advanced-wizard-smoke "SecondBreakfastTobacco")