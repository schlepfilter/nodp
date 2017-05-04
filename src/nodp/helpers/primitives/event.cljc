(ns nodp.helpers.primitives.event
  (:require [cats.protocols :as p]
            [cats.util :as util]
            [com.rpl.specter :as s]
    #?(:cljs [cljs.reader :as reader])
            [nodp.helpers :as helpers]
            [nodp.helpers.time :as time]
            [nodp.helpers.tuple :as tuple])
  #?(:clj
     (:import [clojure.lang IDeref IFn])))

(declare context)

(defn get-occs
  [id network]
  (id (:occs network)))

(defn get-new-time
  [past]
  (let [current (time/now)]
    (if (= past current)
      (get-new-time past)
      current)))

(helpers/defcurried set-occs
                    [occs id network]
                    (s/setval [:occs id s/END] occs network))

(defn modify-network!
  [occ id network]
  ;TODO set modified
  (helpers/call-functions [(set-occs [occ] id)]
                          network))

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
  (#?(:clj  invoke
      :cljs -invoke) [e a]
    ;e stands for an event, and a stands for any as in Push-Pull Functional Reactive Programming.
    (if (:active @helpers/network-state)
      (reset! helpers/network-state
              (modify-network! (tuple/tuple (get-new-time (time/now)) a)
                               (:id e)
                               @helpers/network-state))))
  p/Printable
  (-repr [_]
    (str "#[event " id "]")))

(util/make-printable Event)

(def get-number
  (comp #?(:clj  read-string
           :cljs reader/read-string)
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
  ;TODO add a node to dependency
  (->> @helpers/network-state
       (helpers/call-functions
         (concat [(set-occs [] id)]
                 (map (partial (helpers/flip helpers/funcall) id) fs)))
       (reset! helpers/network-state))
  (Event. id))

(defn event*
  [fs]
  (event** (get-id @helpers/network-state) fs))

(def context
  (helpers/reify-monad
    ;TODO implement monad
    (fn [a]
      (event* [(set-occs [(tuple/tuple (time/time 0) a)])]))
    (fn [])
    ;TODO implement semigroup
    ;TODO implement monoid
    p/Monoid
    (-mempty [_]
             (event* []))))

(def activate
  ;TODO start time
  (juxt (partial swap! helpers/network-state (partial s/setval* :active true))))