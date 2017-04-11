(ns nodp.test.helpers.frp
  (:require [clojure.test.check]
            [clojure.test.check.clojure-test
             :as clojure-test
             :include-macros true]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop :include-macros true]
            [cats.core :as m]
            [cats.monad.maybe :as maybe]
            [cats.protocols :as p]
            [#?(:clj  clojure.core.async
                :cljs cljs.core.async) :as async]
            [#?(:clj  clojure.test
                :cljs cljs.test) :as test :include-macros true]
            [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.primitives.event :as event]
            [nodp.helpers.time :as time]
            [nodp.helpers.tuple :as tuple]
            [nodp.helpers.unit :as unit]
            [nodp.test.helpers :as test-helpers]
    #?(:clj
            [riddley.walk :as walk]))
  #?(:cljs (:require-macros [nodp.test.helpers.frp :refer [restart-for-all
                                                           with-exit
                                                           with-exitv]])))

(defn fixture
  [f]
  (with-redefs [event/queue helpers/funcall]
    (f)))

(test/use-fixtures :once fixture)

(def restart
  (gen/fmap (fn [_]
              (frp/restart))
            (gen/return unit/unit)))

#?(:clj (defmacro restart-for-all
          [bindings & body]
          `(prop/for-all ~(concat `[_# restart]
                                  bindings)
                         ~@body)))

(clojure-test/defspec
  call-inactive
  10
  (restart-for-all [as (gen/vector test-helpers/any-equal)]
                   (let [e (frp/event)]
                     (run! e as)
                     (maybe/nothing? @e))))

(clojure-test/defspec
  call-active
  10
  (restart-for-all [as (gen/vector test-helpers/any-equal)]
                   (let [e (frp/event)]
                     (frp/activate)
                     (run! e as)
                     (= (tuple/snd @@e) (last as)))))

(def probability
  (gen/double* {:max  1
                :min  0
                :NaN? false}))

(clojure-test/defspec
  event-return
  10
  (restart-for-all [a test-helpers/any-equal]
                   (= @@(-> (frp/event)
                            helpers/infer
                            (nodp.helpers/return a))
                      (-> 0
                          time/time
                          (tuple/tuple a)))))

#?(:clj (defmacro with-exit
          [exit-name & body]
          (potemkin/unify-gensyms
            `(let [exit-state## (atom helpers/nothing)]
               ~(walk/walk-exprs
                  (partial = exit-name)
                  (fn [_#]
                    `(comp (partial reset! exit-state##)
                           maybe/just))
                  (cons `do body))
               @@exit-state##))))

(clojure-test/defspec
  with-exit-identity
  10
  (prop/for-all [a test-helpers/any-equal
                 b test-helpers/any-equal]
                (= (with-exit exit
                              (exit a)
                              b)
                   a)))

#?(:clj (defmacro with-exitv
          [exit-name & body]
          (potemkin/unify-gensyms
            `(let [exits-state## (atom [])]
               ~(walk/walk-exprs
                  (partial = exit-name)
                  (fn [_#]
                    `(comp (partial swap! exits-state##)
                           (helpers/curry 2 (helpers/flip conj))))
                  (cons `do body))
               @exits-state##))))

(clojure-test/defspec
  with-exitv-identity
  10
  (prop/for-all [as (gen/vector test-helpers/any-equal)
                 b test-helpers/any-equal]
                (= (with-exitv exit
                               (->> as
                                    (map exit)
                                    doall)
                               b)
                   as)))

(clojure-test/defspec
  on-identity
  10
  (restart-for-all [as (gen/vector test-helpers/any-equal)]
                   (= (with-exitv exit
                                  (let [e (frp/event)]
                                    (frp/on exit e)
                                    (frp/activate)
                                    (run! e as)))
                      as)))

(defn event
  []
  (gen/one-of [(gen/fmap (partial nodp.helpers/return
                                  (helpers/infer (frp/event)))
                         test-helpers/any-equal)
               (gen/return (frp/event))]))

(defn conj-event
  [coll probability*]
  (->> coll
       count
       inc
       (* probability*)
       int
       (if (= 1.0 probability*)
         0)
       (nth (conj coll
                  (test-helpers/generate (event) {:seed (hash probability*)})))
       (conj coll)))

(def get-events
  (partial reduce conj-event []))

(defn probabilities
  ([]
   (-> probability
       gen/vector
       gen/not-empty))
  ([n]
   (gen/vector probability n)))

(defn entities-call*
  [events-tuple-generator]
  (gen/let [[input-events fmapped-events] events-tuple-generator
            xs (->> input-events
                    count
                    (gen/vector gen/boolean))]
           (gen/tuple (gen/return fmapped-events)
                      (gen/return (partial run!
                                           helpers/funcall
                                           (map (fn [e x]
                                                  (fn []
                                                    (if x
                                                      (e unit/unit))))
                                                input-events
                                                xs))))))

(defn events-tuple*
  [events-generator]
  (gen/let [es events-generator
            fs (gen/vector (test-helpers/function test-helpers/any-equal)
                           (count es))]
           (gen/tuple (gen/return es)
                      (gen/return (doall (map nodp.helpers/<$>
                                              fs
                                              es))))))

(def events
  (comp (partial gen/fmap get-events)
        probabilities))

(def events-tuple
  (comp events-tuple*
        events))

(def events-call
  (comp entities-call*
        events-tuple))

(defn make-iterate
  [coll]
  (let [state (atom coll)]
    (fn [& _]
      (let [result (first @state)]
        (swap! state rest)
        result))))

(defn contains-value?
  [coll x]
  (-> coll
      set
      (contains? x)))

(defn contains-event-value?
  [es e]
  (contains-value? (map (comp tuple/snd
                              deref
                              deref) es)
                   (tuple/snd @@e)))

(clojure-test/defspec
  event->>=-nonmember
  5
  (restart-for-all [[fmapped-events call] (events-call)]
                   ;TODO generate event
                   (let [outer-event (frp/event)
                         bound-event (->> fmapped-events
                                          make-iterate
                                          (nodp.helpers/>>= outer-event))]
                     (frp/activate)
                     (dotimes [_ (-> fmapped-events
                                     count
                                     dec)]
                       (outer-event unit/unit))
                     (call)
                     (or (maybe/nothing? @bound-event)
                         (contains-event-value? (drop-last fmapped-events)
                                                bound-event)))))

(def get-time
  (comp tuple/fst
        deref
        deref))

(clojure-test/defspec
  event->>=-delay
  5
  (restart-for-all [inner-events (events)]
                   (let [outer-event (frp/event)
                         bound-event (->> inner-events
                                          make-iterate
                                          (nodp.helpers/>>= outer-event))]
                     (frp/activate)
                     ;TODO interpose calling outer-event and calling input-events
                     (dotimes [_ (-> inner-events
                                     count
                                     dec)]
                       (outer-event unit/unit))
                     ((last inner-events) unit/unit)
                     (outer-event unit/unit)
                     (= (get-time outer-event) (get-time bound-event)))))

(defn all-nothing?
  [e es]
  (and (maybe/nothing? @e)
       (not-any? maybe/just? (map deref es))))

(def get-tuples
  (comp maybe/cat-maybes
        (partial map deref)))

(defn left-biased?*
  [e es]
  (->> es
       get-tuples
       (filter (comp (partial = (apply (partial max-key deref)
                                       (->> es
                                            get-tuples
                                            (map tuple/fst))))
                     tuple/fst))
       first
       tuple/snd
       (= (tuple/snd @@e))))

(def left-biased?
  (helpers/build or
                 all-nothing?
                 left-biased?*))

;TODO avoid blocking
;(def call-units
;  (partial run! (partial (helpers/flip helpers/funcall) unit/unit)))
;
;(clojure-test/defspec
;  event->>=-left-bias
;  5
;  (restart-for-all [[input-events fmapped-events] (events-tuple)]
;                   ;TODO generate event
;                   (let [outer-event (frp/event)
;                         bound-event (->> fmapped-events
;                                          make-iterate
;                                          (nodp.helpers/>>= outer-event))]
;                     (frp/activate)
;                     ;TODO interpose calling outer-event and calling input-events
;                     (dotimes [_ (count fmapped-events)]
;                       (outer-event unit/unit))
;                     (call-units input-events)
;                     (left-biased? bound-event fmapped-events))))

(clojure-test/defspec
  event-<>
  5
  (restart-for-all [[fmapped-events call] (events-call 2)]
                   (let [mappended-event (apply nodp.helpers/<> fmapped-events)]
                     (frp/activate)
                     (call)
                     (left-biased? mappended-event fmapped-events))))

(clojure-test/defspec
  event-mempty
  10
  (restart-for-all [a test-helpers/any-equal]
                   (= @(-> (frp/event)
                           helpers/infer
                           nodp.helpers/mempty)
                      helpers/nothing)))

(def xform
  ;TODO use map to generate similar xforms
  (gen/one-of [(gen/fmap map
                         (test-helpers/function test-helpers/any-equal))
               (gen/fmap mapcat
                         (test-helpers/function test-helpers/any-equal))
               (gen/fmap filter
                         (test-helpers/function gen/boolean))
               (gen/fmap remove
                         (test-helpers/function gen/boolean))]))

(clojure-test/defspec
  transduce-identity
  5
  (restart-for-all [input-event (event)
                    xf xform
                    f (test-helpers/function test-helpers/any-equal)
                    init test-helpers/any-equal
                    as (gen/vector test-helpers/any-equal)]
                   (let [transduced-event (frp/transduce xf f init input-event)
                         earliest @input-event]
                     (frp/activate)
                     (run! input-event as)
                     (->> (maybe/maybe as
                                       earliest
                                       (comp (partial (helpers/flip cons) as)
                                             tuple/snd))
                          (maybe/map-maybe (partial (xf (comp maybe/just
                                                              second
                                                              vector))
                                                    helpers/nothing))
                          (reduce f init)
                          (= (tuple/snd @@transduced-event))))))

(clojure-test/defspec
  behavior-return
  10
  (restart-for-all [a test-helpers/any-equal]
                   (= @(-> unit/unit
                           frp/behavior
                           helpers/infer
                           (nodp.helpers/return a))
                      a)))

(def switcher
  (gen/let [probabilities (gen/sized (comp (partial gen/vector probability 2)
                                           (partial + 2)))
            input-events (gen/fmap get-events
                                   (gen/return probabilities))
            fs (gen/vector (test-helpers/function test-helpers/any-equal)
                           (count input-events))
            fmapped-events (gen/return (doall (map nodp.helpers/<$>
                                                   fs
                                                   input-events)))
            as (->> input-events
                    count
                    (gen/vector test-helpers/any-equal))
            [first-behavior
             return-behavior
             & switched-behaviors
             :as all-behaviors] (gen/return (doall (map frp/stepper
                                                        as
                                                        fmapped-events)))
            switching-event (gen/one-of
                              [(gen/return (frp/event))
                               (gen/return
                                 (helpers/return
                                   (helpers/infer (frp/event))
                                   return-behavior))])
            input-event-calls (gen/shuffle (map (fn [input-event]
                                                  (fn []
                                                    (input-event unit/unit)))
                                                input-events))
            ;TODO interpose calling input-events and calling switching-event without changing the order of switched-behaviors with which to call switching-event
            calls (gen/return
                    (concat input-event-calls
                            (map (fn [switched-behavior]
                                   (fn []
                                     (switching-event switched-behavior)))
                                 switched-behaviors)))]
           (gen/tuple (gen/return (fn []
                                    (run! helpers/funcall calls)))
                      (gen/return (frp/switcher first-behavior switching-event))
                      (gen/return (last (if (maybe/just? @switching-event)
                                          all-behaviors
                                          (cons first-behavior
                                                switched-behaviors)))))))

(clojure-test/defspec
  switcher-last
  5
  (restart-for-all [[call switched-behavior last-behavior] (gen/no-shrink switcher)]
                   (frp/activate)
                   (call)
                   (= @switched-behavior @last-behavior)))

(clojure-test/defspec
  behavior->>=
  5
  (restart-for-all [e (event)
                    f (test-helpers/function test-helpers/any-equal)
                    as (gen/vector test-helpers/any-equal)
                    a test-helpers/any-equal]
                   (let [outer-behavior (frp/stepper a e)
                         bound-behavior (nodp.helpers/>>= outer-behavior
                                                          (comp frp/behavior
                                                                f))]
                     (frp/activate)
                     (run! e as)
                     (= @bound-behavior (f @outer-behavior)))))
