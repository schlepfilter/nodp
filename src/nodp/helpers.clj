(ns nodp.helpers
  (:require [clojure.test :as test]
            [cats.builtin]
            [cats.core :as m]
            [cats.monad.maybe :as maybe]
            [potemkin :as potemkin]
            [riddley.walk :as riddley]))

(defn flip
  [f]
  (fn [x y & more]
    (apply f y x more)))

(defn quote-expr
  [expr]
  `'~expr)

(def quote-seq
  (partial riddley/walk-exprs seq? quote-expr))

(defmacro functionize
  [operator]
  (if (test/function? operator)
    operator
    `(fn [& more#]
       (->> (map quote-seq more#)
            (cons '~operator)
            eval))))

(defmacro build
  [operator & fs]
  `(comp (partial apply (functionize ~operator))
         (juxt ~@fs)))

(defn wrap-maybe
  [expr]
  (if (nil? expr)
    (maybe/nothing)
    (maybe/just expr)))

(defmacro maybe
  [test then]
  `(wrap-maybe (if ~test
                 ~then)))

(defmacro maybe-not
  [test then]
  `(wrap-maybe (if-not ~test
                 ~then)))

(defn ecurry
  [arity f]
  (fn [& outer-more]
    (let [n (count outer-more)]
      (if (== arity n)
        (apply f outer-more)
        (ecurry (- arity n)
                (fn [& inner-more]
                  (apply f (concat outer-more inner-more))))))))

(defmacro curry
  ([f]
   `(m/curry ~f))
  ([arity f]
   `(ecurry ~arity ~f)))

(defn ap
  ([f x]
   (m/<$> f x))
  ([f x & more]
   (apply m/<*>
          (m/<$> (curry (-> more
                            count
                            inc)
                        f)
                 x)
          more)))

(defmacro defmulti-identity
  [mm-name]
  `(defmulti ~mm-name identity))

(defmacro defdefs
  [macro-name macro]
  (potemkin/unify-gensyms
    `(let [ns-macro-name## (resolve '~macro-name)
           ns-macro## (resolve '~macro)]
       (defmacro ~macro-name
         ([])
         ([x## & more##]
           `(do (~ns-macro## ~x##)
                (~ns-macro-name## ~@more##)))))))

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
      (map f-m)
      dorun))

(def printall
  (comp dorun
        (partial map println)))

(defn get-thread-name
  []
  (-> (Thread/currentThread)
      .getName))
