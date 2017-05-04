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
    ;If context is inlined, the following error seems to occur.
    ;java.lang.LinkageError: loader (instance of clojure/lang/DynamicClassLoader): attempted duplicate class definition for name: "nodp/helpers/primitives/event/Event"
    context)
  IFn
  ;TODO implement invoke
  (#?(:clj  invoke
      :cljs -invoke) [e a]
    ;e stands for an event, and a stands for any as in Push-Pull Functional Reactive Programming.
    ))

(def get-number
  (comp read-string
        (partial (helpers/flip subs) 1)
        str))

(helpers/defcurried get-id-number
                    [k network]
                    (if (-> network
                            k
                            empty?)
                      0
                      (-> network
                          k
                          last
                          get-number
                          inc)))

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