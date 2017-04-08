(ns nodp.helpers.tuple
  (:require [cats.core :as m]
            [cats.protocols :as p]
            [cats.util :as util]
            [nodp.helpers :as helpers]))

(declare ->Tuple)
;In ClojureScript declare works around the following warning:
;WARNING: Use of undeclared Var nodp.helpers.tuple/->Tuple

(defrecord Tuple
  [fst snd]
  p/Contextual
  (-get-context [_]
    (helpers/reify-monad
      (partial ->Tuple (nodp.helpers/mempty
                         (helpers/infer fst)))
      (fn [ma f]
        (Tuple. (nodp.helpers/<> (:fst ma)
                                 (:fst (f (:snd ma))))
                (:snd (f (:snd ma)))))))

  p/Printable
  (-repr [_]
    (str "#[tuple " (pr-str fst) " " (pr-str snd) "]")))

(util/make-printable Tuple)

(def snd
  :snd)

(def fst
  :fst)

(defn tuple
  [x y]
  (Tuple. x y))
