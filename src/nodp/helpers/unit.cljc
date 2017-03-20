(ns nodp.helpers.unit
  (:require [cats.protocols :as p]
            [cats.util :as util]))

(defrecord Unit
  []
  p/Printable
  (-repr [_]
    (str "#[unit]")))

(util/make-printable Unit)

(def unit (Unit.))
