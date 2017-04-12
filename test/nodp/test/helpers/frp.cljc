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

(defn make-iterate
  [coll]
  (let [state (atom coll)]
    (fn [& _]
      (let [result (first @state)]
        (swap! state rest)
        result))))

(defn count-left-duplicates
  [coll]
  (dec (count (filter (partial = (first coll)) coll))))

(defn events-tuple
  [probabilities]
  (gen/let [input-events (gen/return (get-events probabilities))
            fs (gen/vector (test-helpers/function test-helpers/any-equal)
                           (count input-events))]
           (gen/tuple
             (gen/return input-events)
             (gen/return (doall (map nodp.helpers/<$>
                                     fs
                                     input-events)))
             (gen/return
               (- ((if (maybe/just? @(first input-events))
                     dec
                     identity)
                    (dec (count input-events)))
                  (count-left-duplicates (get-events probabilities)))))))

(def >>=
  ;TODO refactor
  (gen/let [probabilities (gen/sized (comp (partial gen/vector probability 2)
                                           (partial + 2)))
            [[input-event & input-events]
             [outer-event & inner-events]
             n] (events-tuple probabilities)
            input-event-as (gen/vector test-helpers/any-equal n)
            xs (gen/vector gen/boolean n)
            input-events-as (gen/vector test-helpers/any-equal
                                        (count input-events))
            calls (gen/shuffle
                    (concat (map (fn [x a]
                                   (fn []
                                     (if x
                                       (input-event a))))
                                 xs
                                 input-event-as)
                            ;TODO randomize the number of times input-event is called
                            (map (fn [input-event* a]
                                   (fn []
                                     (input-event* a)))
                                 input-events
                                 input-events-as)))]
           (gen/tuple
             (gen/return outer-event)
             (gen/return inner-events)
             (gen/return (fn []
                           (run! helpers/funcall (drop-last calls))))
             (gen/return (last calls)))))

(defn get-earliest
  [e]
  (event/get-earliest e @helpers/network-state))

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

(defn right-most-earliest?
  [bound-event inner-events]
  (= (tuple/snd @(get-earliest (last inner-events)))
     (tuple/snd @@bound-event)))

(clojure-test/defspec
  event->>=-positive
  5
  (restart-for-all [[outer-event inner-events calls call] >>=]
                   (frp/activate)
                   (let [bound-event (helpers/>>= outer-event
                                                  (make-iterate inner-events))]
                     (calls)
                     (let [outer-latest @outer-event
                           inner-latests (doall (map deref inner-events))
                           bound-latest @bound-event]
                       (call)
                       (if (= (map deref inner-events) inner-latests)
                         (if (and (not= @outer-event outer-latest)
                                  (maybe/just? @(last inner-events)))
                           (right-most-earliest? bound-event inner-events)
                           (= @bound-event bound-latest))
                         (if (and (not= @outer-event outer-latest)
                                  (= (map deref (drop-last inner-events))
                                     (drop-last inner-latests)))
                           (right-most-earliest? bound-event inner-events)
                           (left-biased? bound-event inner-events)))))))

(def <>
  (gen/let [probabilities (gen/vector probability 2)
            [input-events fmapped-events] (events-tuple probabilities)
            ;TODO randomize the number of times each input-event is called
            calls (gen/shuffle (map (fn [e]
                                      (fn []
                                        (e unit/unit)))
                                    input-events))]
           (gen/tuple (gen/return (partial run!
                                           helpers/funcall
                                           calls))
                      (gen/return (apply helpers/<> fmapped-events))
                      (gen/return fmapped-events))))

(clojure-test/defspec
  event-<>
  5
  (restart-for-all [[call mappended-event fmapped-events] <>]
                   (frp/activate)
                   (call)
                   (left-biased? mappended-event fmapped-events)))

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
            [input-events fmapped-events] (events-tuple probabilities)
            as (->> input-events
                    count
                    (gen/vector test-helpers/any-equal))
            ;TODO randomly create each behavior either by using time or stepper
            [first-behavior
             return-behavior
             & switched-behaviors
             :as all-behaviors] (gen/return (doall (map frp/stepper
                                                        as
                                                        fmapped-events)))
            ;TODO randomize the simultaneity of input-events and switching-event
            switching-event (gen/one-of
                              [(gen/return (frp/event))
                               (gen/return
                                 (helpers/return
                                   (helpers/infer (frp/event))
                                   return-behavior))])
            ;TODO randomize the number of times each input-event is called
            input-event-calls (gen/shuffle (map (fn [input-event]
                                                  (fn []
                                                    (input-event unit/unit)))
                                                input-events))
            ;TODO randomize the order of calling input-events and calling switching-event without changing the order of switched-behaviors with which to call switching-event
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
  (restart-for-all [[call switched-behavior last-behavior] switcher]
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
