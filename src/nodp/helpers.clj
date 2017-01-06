(ns nodp.helpers
  (:require [clojure.string :as str]
            [clojure.test :as test]
            [beicon.core :as rx]
            [cats.builtin]
            [cats.core :as m]
            [cats.monad.maybe :as maybe]
            [clojurewerkz.money.amounts :as ma]
            [clojurewerkz.money.currencies :as mc]
            [com.rpl.specter :as s]
            [potemkin :as potemkin]))

(defn call-pred
  ([_]
   true)
  ([pred expr]
   (pred expr)))

(defmacro casep
  [x & clauses]
  `(condp call-pred ~x
     ~@clauses))

(defmacro case-eval
  [x & clauses]
  `(condp = ~x
     ~@clauses))

(defn flip
  [f]
  (fn
    ([x]
     (fn [y & more]
       (apply f y x more)))
    ([x y & more]
     (apply f y x more))))

(defn quote-expr
  [expr]
  `'~expr)

(def Seqs
  (s/recursive-path [] p
                    (s/cond-path seq? s/STAY
                                 coll? [s/ALL p]
                                 :else s/STOP)))

(def quote-seq
  (partial s/transform* Seqs quote-expr))

;This definition results in an error.
;(def quote-seq
;  (partial riddley/walk-exprs seq? quote-expr))
;
;((build (partial s/transform* :a) (constantly inc) identity) {:a 0})
;java.lang.ExceptionInInitilizerError
;
;This may be because
;(quote-seq +)
;=>
;#object[clojure.lang.AFunction$1 0xc6687f0 "clojure.lang.AFunction$1@c6687f0"]
;whereas it is expected that
;(quote-seq +)
;=>
;#object[clojure.core$_PLUS_ 0x3bc719a3 "clojure.core$_PLUS_@3bc719a3"]

(defn gensymize
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
  (-> `(def ~(gensym) ~x)
      eval
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
            (->> (map (comp gensymize quote-seq) more#)
                 (cons '~operator)
                 eval))))

(defmacro build
  [operator & fs]
  `(comp (partial apply (functionize ~operator))
         (juxt ~@fs)))

;This definition is harder to read.
;This definition doesn't use functionize.
;(defmacro build
;  [operator & fs]
;  (potemkin/unify-gensyms
;    `(fn [& more##]
;       (~operator ~@(map (fn [f##]
;                           `(apply ~f## more##))
;                         fs)))))

(defn- get-required-arity
  [f]
  (-> (exc/try-or-recover (-> f
                              .getRequiredArity
                              maybe/just)
                          (fn [_] (-> (maybe/nothing)
                                      (exc/success))))
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
        get-arities))

(defn curry
  ([f]
   (curry f (get-currying-arity f)))
  ([f arity]
   (fn [& outer-more]
     (let [n (count outer-more)]
       (case-eval arity
                  n (apply f outer-more)
                  (curry (fn [& inner-more]
                           (apply f (concat outer-more inner-more)))
                         (- arity n)))))))

(defn maybe
  [expr]
  (casep expr
         nil? (maybe/nothing)
         (maybe/just expr)))

(defmacro maybe-if
  [test then]
  `(maybe (if ~test
            ~then)))

(defmacro maybe-if-not
  [test then]
  `(maybe (if-not ~test
            ~then)))

(def comp-just
  (partial comp maybe/just))

(defmacro defpfmethod
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
                (~qualified-macro-name## ~@more##)))))))

(def space-join
  (partial str/join " "))

(def comma-join
  (partial str/join ", "))

(defmacro defmulti-identity
  [mm-name]
  `(defmulti ~mm-name identity))

(defdefs defmultis-identity
         defmulti-identity)

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

(defn get-thread-name
  []
  (-> (Thread/currentThread)
      .getName))

(defn make-add-action
  [f]
  (build (partial s/setval* [:actions s/END])
         (comp vector f)
         identity))

;This definition is less readable.
;(defn make-add-action
;  [f]
;  (build (partial s/transform* :actions)
;         (comp (flip (curry 2 conj))
;               f)
;         identity))

(def get-usd
  (partial ma/amount-of mc/USD))
