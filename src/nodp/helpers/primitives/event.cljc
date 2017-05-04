(ns nodp.helpers.primitives.event
  (:require [cats.protocols :as p]
            [cats.util :as util]
            [com.rpl.specter :as s]
            [nodp.helpers :as helpers])
  #?(:clj
     (:import [clojure.lang IDeref IFn])))

(declare context)

(defn get-occs
  [id network]
  (id (:occs network)))

(defrecord Event
  [id]
  p/Contextual
  (-get-context [_]
    ;If context is inlined, the following error seems to occur.
    ;java.lang.LinkageError: loader (instance of clojure/lang/DynamicClassLoader): attempted duplicate class definition for name: "nodp/helpers/primitives/event/Event"
    context)
  IDeref
  (#?(:clj  deref
      :cljs -deref) [e]
    (get-occs (:id e) @helpers/network-state))
  IFn
  ;TODO implement invoke
  (#?(:clj  invoke
      :cljs -invoke) [e a]
    ;e stands for an event, and a stands for any as in Push-Pull Functional Reactive Programming.
    )
  p/Printable
  (-repr [_]
    (str "#[event " id "]")))

(util/make-printable Event)

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
                          key
                          get-number
                          inc)))

(def get-id
  (helpers/build (comp keyword
                       str
                       max)
                 (get-id-number :occs)
                 (get-id-number :function)))

(defn event**
  [id fs]
  ;TODO call fs
  (->> @helpers/network-state
       (helpers/call-functions [(partial s/setval* [:occs id] [])])
       (reset! helpers/network-state))
  (Event. id))

(defn event*
  [fs]
  (event** (get-id @helpers/network-state) fs))

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

(def activate
  ;TODO start time
  (juxt (partial swap! helpers/network-state (partial s/setval* :active true))))