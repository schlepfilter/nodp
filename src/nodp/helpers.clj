(ns nodp.helpers
  (:require [riddley.walk :as riddley]))

(defn flip
  [f]
  (fn
    ([] (f))
    ([x] (f x))
    ([x y & more] (apply f y x more))))

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
  `(comp
     (partial apply (functionize ~operator))
     (juxt ~@fs)))

(def printall
  (comp dorun
        (partial map println)))

