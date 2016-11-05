(ns nodp.helpers
  (:require [riddley.walk :as riddley]))

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

(defn- defmulti-identity
  [mm-name]
  (-> `(defmulti ~mm-name identity)
      eval))

(def defmultis-identity
  (comp dorun
        (partial map defmulti-identity)))

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
