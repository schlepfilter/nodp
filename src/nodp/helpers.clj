(ns nodp.helpers
  (:require [cats.monad.maybe :as maybe]
            [riddley.walk :as riddley]))

(defn flip
  [f]
  (fn [x y & more]
    (apply f y x more)))

(defn quote-form
  [form]
  `'~form)

(def quote-seq
  (partial riddley/walk-exprs seq? quote-form))

(defmacro functionize
  [operator]
  `(fn [& args#]
     (->> (map quote-seq args#)
          (cons '~operator)
          eval)))

(defmacro build
  [operator & fs]
  `(comp (partial apply (functionize ~operator))
         (juxt ~@fs)))

(defmacro defmulti-identity
  [mm-name]
  `(defmulti ~mm-name identity))

(defmacro defmultis-identity
  ([])
  ([x & more]
   `(do (defmulti-identity ~x)
        (defmultis-identity ~@more))))

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

(def print-constantly
  (comp (partial comp println)
        constantly))

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
