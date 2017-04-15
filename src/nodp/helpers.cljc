(ns nodp.helpers
  (:refer-clojure :exclude [defcurried])
  (:require [clojure.string :as str]
            [beicon.core :as rx]
            [cats.context :as ctx]
            [cats.core :as m]
            [cats.monad.exception :as exc]
            [cats.monad.maybe :as maybe]
            [cats.protocols :as p]
            [#?(:clj  clojure.core.async
                :cljs cljs.core.async) :as async]
            [com.rpl.specter :as s]
            [loom.alg :as alg]
            [loom.graph :as graph]
    #?@(:clj [
            [clojure.test :as test]
            [clojurewerkz.money.amounts :as ma]
            [clojurewerkz.money.currencies :as mc]
            [potemkin :as potemkin]])
            [nodp.helpers.unit :as unit])
  #?(:cljs (:require-macros [cljs.core.async.macros :as async]
             [nodp.helpers :refer [build case-eval casep defcurried mlet]])))

(defn call-pred
  ([_]
   true)
  ([pred expr]
   (pred expr)))

#?(:clj
   (do (defmacro casep
         [x & clauses]
         `(condp call-pred ~x
            ~@clauses))

       (defmacro case-eval
         [x & clauses]
         `(condp = ~x
            ~@clauses))))

(defn flip
  [f]
  (fn
    ([x]
     (fn [y & more]
       (apply f y x more)))
    ([x y & more]
     (apply f y x more))))

#?(:clj
   (do (defn gensymize
         ;This function works around java.lang.ExceptionInInitializerError
         ;(eval (list map (partial + 1) [0]))
         ;CompilerException java.lang.ExceptionInInitializerError
         ;(eval (list map (def x (partial + 1)) [0]))
         ;=> (1)
         ;(eval (list map inc [0]))
         ;=> (1)
         ;(eval (list map (fn [x] (+ 1 x)) [0]))
         ;=> (1)
         [x]
         (-> (intern *ns* (gensym) x)
             str
             (subs 2)
             symbol))

       (defmacro functionize
         ;If operator is a list, then it returns a value, which can be passed arround.
         [operator]
         (casep operator
                test/function? operator
                list? operator
                `(fn [& more#]
                   (->> (map gensymize more#)
                        (cons '~operator)
                        eval))))

       ;This definition is harder to read.
       ;This definition doesn't use functionize.
       (defmacro build
         [operator & fs]
         (potemkin/unify-gensyms
           `(fn [& more##]
              (~operator ~@(map (fn [f##]
                                  `(apply ~f## more##))
                                fs)))))

       ;This defintion is not compatible with ClojureScript
       ;(defmacro build
       ;  [operator & fs]
       ;  `(comp (partial apply (functionize ~operator))
       ;         (juxt ~@fs)))

       (defn- get-required-arity
         [f]
         (-> (exc/try-or-recover (-> f
                                     .getRequiredArity
                                     maybe/just)
                                 (fn [_]
                                   (exc/success unit/unit)))
             m/join))

       (def get-non-variadic-arities
         (comp (partial map (comp alength
                                  (functionize .getParameterTypes)))
               (partial filter (comp (partial = "invoke")
                                     (functionize .getName)))
               (functionize .getDeclaredMethods)
               class))

       (def get-arities
         (build (comp distinct
                      maybe/cat-maybes
                      cons)
                get-required-arity
                (comp (partial map maybe/just)
                      get-non-variadic-arities)))

       (def get-currying-arity
         (comp (partial max 2)
               (partial apply min)
               get-arities))))

(defn curry
  #?(:clj ([f]
           (curry (get-currying-arity f) f)))
  ([arity f]
   (fn [& outer-more]
     (let [n (count outer-more)]
       (case-eval arity
                  n (apply f outer-more)
                  (curry (- arity n)
                         (fn [& inner-more]
                           (apply f (concat outer-more inner-more)))))))))

#?(:clj (defmacro defcurried
          [function-name bindings & body]
          `(def ~function-name
             (curry ~(count bindings)
                    (fn ~bindings
                      ~@body)))))

(defn infer
  "Given an optional value infer its context. If context is already set, it
  is returned as is without any inference operation."
  {:no-doc true}
  ([]
   (when (nil? ctx/*context*)
     (ctx/throw-illegal-argument "No context is set."))
   ctx/*context*)
  ([v]
   (cond
     (satisfies? p/Contextual v)
     (p/-get-context v)
     :else
     (ctx/throw-illegal-argument
       (str "No context is set and it can not be automatically "
            "resolved from provided value")))))

;TODO remove this function after cats.context is fixed
(defn <>
  [& more]
  (with-redefs [cats.context/infer infer]
    (apply m/<> more)))

;TODO remove this function after cats.context is fixed
(defn mempty
  [& more]
  (with-redefs [cats.context/infer infer]
    (apply m/mempty more)))

;TODO remove this function after cats.context is fixed
(defn <$>
  [& more]
  (with-redefs [cats.context/infer infer]
    (apply m/<$> more)))

;TODO remove this function after cats.context is fixed
(defn pure
  [& more]
  (with-redefs [cats.context/infer infer]
    (apply m/pure more)))

;TODO remove this function after cats.context is fixed
(defn <*>
  [& more]
  (with-redefs [cats.context/infer infer]
    (apply m/<*> more)))

;TODO remove this function after cats.context is fixed
(defn return
  [& more]
  (with-redefs [cats.context/infer infer]
    (apply m/return more)))

;TODO remove this function after cats.context is fixed
(defn >>=
  [& more]
  (with-redefs [cats.context/infer infer]
    (apply m/>>= more)))

;TODO remove this function after cats.context is fixed
(defn =<<
  [& more]
  (with-redefs [cats.context/infer infer]
    (apply m/=<< more)))

;TODO remove this macro after cats.context is fixed
#?(:clj (defmacro lift-a
          [& more]
          `(with-redefs [cats.context/infer infer]
             (m/lift-a ~@more))))

;TODO remove this macro after cats.context is fixed
#?(:clj (defmacro lift-m
          [& more]
          `(with-redefs [cats.context/infer infer]
             (m/lift-m ~@more))))

;TODO remove this macro after cats.context is fixed
#?(:clj (defmacro mlet
          [& more]
          `(with-redefs [cats.context/infer infer]
             (m/mlet ~@more))))

;TODO remove this macro after cats.context is fixed
#?(:clj (defmacro ->=
          [& more]
          `(with-redefs [cats.context/infer infer]
             (m/->= ~@more))))

(defn ap
  [m1 m2]
  (mlet [x1 m1
         x2 m2]
        (return (x1 x2))))

#?(:clj (defmacro reify-monad
          [pure mbind & more]
          `(reify
             p/Context
             p/Functor
             (~'-fmap [_# f# fa#]
               ((nodp.helpers/lift-m 1 f#) fa#))
             p/Applicative
             (~'-pure [_# v#]
               (~pure v#))
             (~'-fapply [_# fab# fa#]
               (ap fab# fa#))
             p/Monad
             (~'-mreturn [_# a#]
               (~pure a#))
             (~'-mbind [_# ma# f#]
               (~mbind ma# f#))
             ~@more)))

(def nothing
  (maybe/nothing))

(defn maybe*
  [expr]
  (casep expr
         nil? nothing
         (maybe/just expr)))

#?(:clj
   (do (defmacro maybe-if
         [test then]
         `(maybe* (if ~test
                    ~then)))

       (defmacro maybe-if-not
         [test then]
         `(maybe* (if-not ~test
                    ~then)))))

(def comp-just
  (partial comp maybe/just))

(def network-state
  (atom {}))

(defn get-queue
  [f]
  ;TODO handle exceptions
  (let [c (async/chan)]
    (async/go-loop []
      (let [x (async/<! c)]
        (f x))
      (recur))
    c))

(defn funcall
  ([f]
   (f))
  ([f & more]
   (apply f more)))

(defcurried make-get
            [k entity network]
            ((:id entity) (k network)))

(def get-latest
  (make-get :latest))

(def get-keyword
  (comp keyword
        str/lower-case
        last
        (partial (flip str/split) #?(:clj  #"\."
                                     :cljs #"/"))
        ;JavaScript doesn't seem to implement lookbehind.
        ;=> (partial re-find #"(?<=\.)\w*$")
        ;#object[SyntaxError SyntaxError: Invalid regular expression: /(?<=\.)\w*$/: Invalid group]
        pr-str
        type))

(def make-add-node
  (build (m/curry s/transform*)
         (comp (partial vector :dependency)
               get-keyword)
         (comp (flip graph/add-nodes)
               :id)))

(defn comp-entity-functions
  [entity-name fs]
  (cons `comp
        (map (partial (flip list) entity-name)
             fs)))

(def call-functions
  (flip (partial reduce (flip funcall))))

(defcurried modify-entity!
            [entity network]
            (call-functions ((:id entity) (:modifier network))
                            network))

(defcurried set-modifier-empty
            [entity network]
            (s/setval [:modifier (:id entity)] [] network))

#?(:clj (defmacro get-entity
          [entity-name constructor & fs]
          `(let [~entity-name (-> network-state
                                  (swap! (partial s/transform* :id inc))
                                  :id
                                  str
                                  keyword
                                  ~constructor)]
             (reset! network-state
                     (~(comp-entity-functions entity-name
                                              (concat [`modify-entity!]
                                                      fs
                                                      ;set-modifier-empty can be removed.
                                                      ;set-modifier-empty is called to avoid nil.
                                                      [`set-modifier-empty
                                                       `make-add-node]))
                       @network-state))
             ~entity-name)))

(defcurried add-edge
            [parent child network]
            (s/transform
              [:dependency (get-keyword parent)]
              (partial (flip graph/add-edges) (map :id [parent child]))
              network))

(defcurried set-modifier
            [f entity network]
            (s/setval [:modifier (:id entity) s/END]
                      [f]
                      network))

(defcurried set-latest
            ;The order of a and entity is consistent with the parameters of primitives.
            ;entity stands for an event or behavior.
            [a entity network]
            (s/setval [:latest (:id entity)] a network))

(defn get-reachable-subgraph
  [g n]
  (->> n
       (alg/bf-traverse g)
       (graph/subgraph g)))

(defn get-ancestor-subgraph
  [entity network]
  (-> network
      :dependency
      ((get-keyword entity))
      graph/transpose
      (get-reachable-subgraph (:id entity))
      (graph/remove-nodes (:id entity))
      graph/transpose))

(defn get-parent-ancestor-modifiers
  [entity network]
  (mapcat (:modifier network)
          (alg/topsort (get-ancestor-subgraph entity network))))

(defn modify-parent-ancestor!
  [entity network]
  (call-functions (get-parent-ancestor-modifiers entity network) network))

(defn effect-swap!
  [a f]
  (reset! a (f @a)))

(defn effect-swap-entity!
  [entity]
  (run! (fn [f]
          (effect-swap! network-state (partial f entity)))
        [modify-parent-ancestor! modify-entity!]))

#?(:clj
   (do (defmacro defpfmethod
         [multifn dispatch-val f]
         `(defmethod ~multifn ~dispatch-val
            [& x#]
            (apply ~f x#)))

       (defmacro defdefs
         [macro-name macro]
         (potemkin/unify-gensyms
           `(let [qualified-macro-name## (resolve '~macro-name)
                  qualified-macro## (resolve '~macro)]
              (defmacro ~macro-name
                ([])
                ([x## & more##]
                  `(do (~qualified-macro## ~x##)
                       (~qualified-macro-name## ~@more##)))))))))

(def space-join
  (partial str/join " "))

(def comma-join
  (partial str/join ", "))

#?(:clj
   (do (defmacro defmulti-identity
         [mm-name]
         `(defmulti ~mm-name identity))

       (defdefs defmultis-identity
                defmulti-identity)))

(defn- make-defmethod
  [dispatch-val]
  (fn [[k v]]
    (defmethod k dispatch-val
      [_]
      v)))

(defn defmethods
  [dispatch-val f-m]
  (-> (make-defmethod dispatch-val)
      (run! f-m)))

(def printall
  (partial run! println))

(def printstream
  (partial (flip rx/on-next) println))

#?(:clj
   (defn get-thread-name
     []
     (-> (Thread/currentThread)
         .getName)))

(defn transfer*
  [apath transfer-fn structure]
  ((build (partial s/setval* apath)
          transfer-fn
          identity)
    structure))

(defn make-add-action
  [f]
  (partial transfer*
           [:actions s/END]
           (comp vector
                 f)))

;This definition is less readable.
;(defn make-add-action
;  [f]
;  (build (partial s/transform* :actions)
;         (comp (flip (curry conj 2))
;               f)
;         identity))

#?(:clj
   (def get-usd
     (partial ma/amount-of mc/USD)))
