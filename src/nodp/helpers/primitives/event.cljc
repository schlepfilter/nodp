(ns nodp.helpers.primitives.event
  (:require [cats.protocols :as p]
            [nodp.helpers :as helpers])
  #?(:clj
     (:import [clojure.lang IFn])))

(declare context)

(defrecord Event
  [id]
  p/Contextual
  (-get-context [_]
    context)
  IFn
  ;TODO implement invoke
  (#?(:clj  invoke
      :cljs -invoke) [e a]))

(def get-number
  (comp read-string
        (partial (helpers/flip subs) 1)
        str))

(helpers/defcurried get-id-number
                    [k network]
                    (if (empty? (k network))
                      0
                      (inc (get-number (last (k network))))))

(def get-id
  (helpers/build (comp keyword
                       str
                       max)
                 (get-id-number :occs)
                 (get-id-number :function)))

(defn event*
  ;TODO call fs
  [fs]
  (Event. (get-id @helpers/network-state)))

(def context
  (helpers/reify-monad
    ;TODO implement monad
    (fn [])
    (fn [])
    ;TODO implement semigroup
    ;TODO implement monoid
    p/Monoid
    (-mempty [_]
             (event* []))))