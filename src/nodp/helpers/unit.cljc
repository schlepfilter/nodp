(ns nodp.helpers.unit
  (:require [cats.protocols :as p]
            [cats.util :as util]))

(defrecord Unit
  []
  p/Contextual
  (-get-context [_]
    (reify
      p/Context
      p/Semigroup
      (-mappend [_ _ _]
        (Unit.))
      p/Monoid
      (-mempty [_]
        (Unit.))))
  p/Printable
  (-repr [_]
    (str "#[unit]")))

(util/make-printable Unit)

(def unit (Unit.))
