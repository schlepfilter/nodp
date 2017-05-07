;event and behavior namespaces are separated to limit the impact of :refer-clojure :exclude for transduce
(ns nodp.helpers.primitives.event
  (:require [cats.protocols :as p]
            [cats.util :as util]
            [com.rpl.specter :as s]
            [linked.core :as linked]
            [loom.alg :as alg]
            [loom.graph :as graph]
    #?(:cljs [cljs.reader :as reader])
            [nodp.helpers :as helpers]
            [nodp.helpers.time :as time]
            [nodp.helpers.tuple :as tuple])
  #?(:clj
     (:import [clojure.lang IDeref IFn])))

(declare context)

(defn get-initial-network
  []
  {:dependency (graph/digraph)
   :function   (linked/map)
   :occs       (linked/map)
   :time       (time/time 0)})

(def network-state
  (atom (get-initial-network)))

(defn get-occs
  [id network]
  (-> network
      :occs
      id))

(defn get-new-time
  [past]
  (let [current (time/now)]
    (if (= past current)
      (get-new-time past)
      current)))

(defn get-times
  []
  (let [past (time/now)]
    [past (get-new-time past)]))

(helpers/defcurried set-occs
                    [occs id network]
                    (s/setval [:occs id s/END] occs network))

(defn modify-network!
  [occ id network]
  ;TODO advance
  (helpers/call-functions
    (concat [(partial s/setval* [:modified s/MAP-VALS] false)
             ;TODO clear cache
             (partial s/setval* :time (tuple/fst occ))
             (set-occs [occ] id)
             (partial s/setval* [:modified id] true)]
            (mapcat (:modifies! network) (alg/topsort (:dependency network))))
    network))

(def run-effects!
  (helpers/build run!
                 (helpers/curry 2 (helpers/flip helpers/funcall))
                 :effects))

(defrecord Event
  [id]
  p/Contextual
  (-get-context [_]
    ;If context is inlined, the following error seems to occur.
    ;java.lang.LinkageError: loader (instance of clojure/lang/DynamicClassLoader): attempted duplicate class definition for name: "nodp/helpers/primitives/event/Event"
    context)
  IFn
  (#?(:clj  invoke
      :cljs -invoke) [_ a]
    ;e stands for an event, and a stands for any as in Push-Pull Functional Reactive Programming.
    (when (:active @network-state)
      ;TODO call run-effects! twice with different times
      (let [[past current] (get-times)]
        ((juxt (partial reset! network-state)
               run-effects!)
          (modify-network! (tuple/tuple past a)
                           id
                           @network-state))
        (->> (partial s/setval* :time current)
             (swap! network-state))
        (run-effects! @network-state))))
  IDeref
  (#?(:clj  deref
      :cljs -deref) [_]
    (get-occs id @network-state))
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
                 (map ((helpers/curry 3 (helpers/flip helpers/funcall)) id)
                      fs)))
       (reset! network-state))
  (Event. id))

(defn event*
  [fs]
  (event** (get-id @network-state) fs @network-state))

(helpers/defcurried add-edge
                    [parent-id child-id network]
                    (s/transform :dependency
                                 (partial (helpers/flip graph/add-edges)
                                          [parent-id child-id])
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
          (effect-swap! network-state (partial f id)))
        [modify-parent-ancestor! modify-event!]))

(defn make-call-once
  [id modify!]
  (fn [network]
    (if (-> network
            :modified
            id)
      network
      (modify! network))))

(def snth
  (comp (partial apply s/srange)
        (partial repeat 2)))

(defn insert-modify
  [modify! id network]
  (s/setval [:modifies! id (-> network
                               :modifies!
                               id
                               count
                               (- 2)
                               snth)]
            [(make-call-once id modify!)]
            network))

(helpers/defcurried
  insert-merge-sync
  [parent-id child-id network]
  (insert-modify (fn [network*]
                   (set-occs (get-latests parent-id network*)
                             child-id
                             network*))
                 child-id
                 network))

(defn delay-time-occs
  [t occs]
  (map (partial nodp.helpers/<*>
                (tuple/tuple t identity))
       occs))

(helpers/defcurried
  delay-sync
  [parent-id child-id network]
  (set-occs (delay-time-occs (:time network) (get-occs parent-id network))
            child-id
            network))

(helpers/defcurried modify->>=
                    [parent-id f initial child-id network]
                    (do
                      (reset! network-state network)
                      (let [parent-events
                            (->> network
                                 ((make-get-occs-or-latests initial) parent-id)
                                 (map (comp f
                                            tuple/snd))
                                 doall)]
                        (run! (comp effect-swap-event!
                                    :id)
                              parent-events)
                        (helpers/call-functions
                          (map (comp (fn [parent-id*]
                                       (partial helpers/call-functions
                                                ((juxt add-edge
                                                       insert-merge-sync
                                                       delay-sync)
                                                  parent-id*
                                                  child-id)))
                                     :id)
                               parent-events)
                          @network-state))))

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

(defn merge-one
  [parent merged]
  (s/setval s/END [(first parent)] merged))

(def get-first-time-number
  (comp deref
        tuple/fst
        first))

(defn merge-occs*
  [merged left right]
  (cond (empty? left) (s/setval s/END right merged)
        (empty? right) (s/setval s/END left merged)
        (<= (get-first-time-number left) (get-first-time-number right))
        (recur (merge-one left merged) (rest left) right)
        :else
        (recur (merge-one right merged) left (rest right))))

(def merge-occs
  (partial merge-occs* []))

(helpers/defcurried modify-<>
                    [left-id right-id initial child-id network]
                    (set-occs (merge-occs ((make-get-occs-or-latests initial)
                                            left-id
                                            network)
                                          ((make-get-occs-or-latests initial)
                                            right-id
                                            network))
                              child-id
                              network))

(def context
  (helpers/reify-monad
    (comp event*
          vector
          set-occs
          vector
          (partial tuple/tuple (time/time 0)))
    (fn [ma f]
      (->> (modify->>= (:id ma) f)
           make-set-modify-modify
           (cons (add-edge (:id ma)))
           event*))
    p/Semigroup
    (-mappend [_ left-event right-event]
              (-> (modify-<> (:id left-event)
                             (:id right-event))
                  make-set-modify-modify
                  (concat (map (comp add-edge
                                     :id)
                               [left-event right-event]))
                  event*))
    p/Monoid
    (-mempty [_]
             (event* []))))

(def activate
  (juxt (partial swap! network-state (partial s/setval* :active true))
        (fn []
          (run-effects! @network-state))
        time/start))
