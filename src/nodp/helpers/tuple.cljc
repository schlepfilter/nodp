(ns nodp.helpers.tuple
  (:require [cats.protocols :as p]
            [cats.util :as util]
            [help.core :as help]
            [nodp.helpers :as helpers]))

(declare ->Tuple)
;In ClojureScript declare works around the following warning:
;WARNING: Use of undeclared Var nodp.helpers.tuple/->Tuple

(defrecord Tuple
  [fst snd]
  p/Contextual
  (-get-context [_]
    (helpers/reify-monad
      (partial ->Tuple (-> fst
                           help/infer
                           help/mempty))
      (fn [_ f]
        (Tuple. (->> snd
                     f
                     :fst
                     (help/<> fst))
                (-> snd
                    f
                    :snd)))))

  p/Printable
  (-repr [_]
    (str "#[tuple " (pr-str fst) " " (pr-str snd) "]")))

(util/make-printable Tuple)

(def tuple
  ->Tuple)

(def snd
  :snd)

(def fst
  :fst)
