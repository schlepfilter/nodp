(ns nodp.helpers.primitives.event
  (:require [cats.protocols :as p]
            [cats.util :as util]
            [com.rpl.specter :as s]
            [loom.alg :as alg]
            [loom.graph :as graph]
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
  [id fs network]
  ;TODO add a node to dependency
  (->> network
       (helpers/call-functions
         (concat [(set-occs [] id)]
                 (map (fn [f] (partial f id)) fs)))
       (reset! helpers/network-state))
  (Event. id))

(defn event*
  [fs]
  (event** (get-id @helpers/network-state) fs @helpers/network-state))

(helpers/defcurried add-edge
                    [id e network]
                    (s/transform :dependency
                                 (partial (helpers/flip graph/add-edges)
                                          [id (:id e)])
                                 network))

(defn get-latests
  [id network]
  (reverse (take-while (comp (partial = (:time network))
                             tuple/fst)
                       (rseq (get-occs id network)))))

(defn make-get-occs-or-latests
  [initial]
  (if initial
    get-occs
    get-latests))

(defn effect-swap!
  [state f]
  (->> @state
       f
       (reset! state)))

(defn get-reachable-subgraph
  [g n]
  (->> n
       (alg/bf-traverse g)
       (graph/subgraph g)))

(defn get-ancestor-subgraph
  [id network]
  (-> network
      :dependency
      graph/transpose
      (get-reachable-subgraph id)
      (graph/remove-nodes id)
      graph/transpose))

(defn get-parent-ancestor-modifies
  [id network]
  (->> network
       (get-ancestor-subgraph id)
       alg/topsort
       (mapcat (:modifies! network))))

(defn modify-parent-ancestor!
  [id network]
  (helpers/call-functions (get-parent-ancestor-modifies id network) network))

(helpers/defcurried modify-event!
                    [id network]
                    (helpers/call-functions (id (:modifies! network)) network))

(defn effect-swap-event!
  [id]
  (run! (fn [f]
          (effect-swap! helpers/network-state (partial f id)))
        [modify-parent-ancestor! modify-event!]))

(defn make-call-once
  [id modify!]
  (fn [network]
    (if (id (:modified network))
      network
      (modify! network))))

(defn insert-modify
  [modify! id network]
  (->> network
       (s/transform [:modifies! id] drop-last)
       (s/setval [:modifies! id s/END]
                 [(make-call-once id modify!)
                  (last (id (:modifies! network)))])))


(helpers/defcurried
  insert-merge-sync
  [id e network]
  (insert-modify (fn [network*]
                   (set-occs (get-latests id network*) e network*))
                 e
                 network))

(helpers/defcurried
  delay-sync
  [id e network]
  (set-occs (map (partial nodp.helpers/<*>
                          (tuple/tuple (:time network) identity))
                 (get-occs id network))
            e
            network))

(helpers/defcurried modify->>=
                    [ma f initial child-event network]
                    (do (reset! helpers/network-state network)
                        (let [parent-events
                              (->> network
                                   ((make-get-occs-or-latests initial) ma)
                                   (map (comp f
                                              tuple/snd))
                                   doall)]
                          (run! (comp effect-swap-event!
                                      :id)
                                parent-events)
                          (helpers/call-functions
                            (map (comp (fn [id]
                                         (partial helpers/call-functions
                                                  ((juxt add-edge
                                                         insert-merge-sync
                                                         delay-sync)
                                                    id
                                                    child-event)))
                                       :id)
                                 parent-events)
                            @helpers/network-state))))

(defn set-modify
  [id modify! network]
  (s/setval [:modifies! id]
            [(make-call-once id modify!)
             (partial s/setval* [:modified id] true)]
            network))

(defn make-set-modify-modify
  [modify*]
  [(fn [id network]
     (set-modify id
                 (modify* false id)
                 network))
   (modify* true)])

(def context
  (helpers/reify-monad
    (fn [a]
      (event* [(set-occs [(tuple/tuple (time/time 0) a)])]))
    (fn [ma f]
      (->> (modify->>= (:id ma) f)
           make-set-modify-modify
           (cons (add-edge (:id ma)))
           event*))
    ;TODO implement semigroup
    ;TODO implement monoid
    p/Monoid
    (-mempty [_]
             (event* []))))

(def activate
  ;TODO start time
  (juxt (partial swap! helpers/network-state (partial s/setval* :active true))))