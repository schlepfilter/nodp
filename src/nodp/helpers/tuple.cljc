(ns nodp.helpers.tuple
  (:require [cats.protocols :as p]
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
      (fn [_ f]
        (Tuple. (nodp.helpers/<> fst
                                 (:fst (f snd)))
                (:snd (f snd))))))

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
