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
      ;TODO use cats.context/infer after cats.context is fixed
      (partial ->Tuple (m/mempty
                         (with-redefs [cats.context/infer helpers/infer]
                           (cats.context/infer fst))))
      (fn [ma f]
        ;TODO use cats.context/infer after cats.context is fixed
        (Tuple. (with-redefs [cats.context/infer helpers/infer]
                  (m/mappend (:fst ma)
                             (:fst (f (:snd ma)))))
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
