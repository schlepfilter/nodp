(ns nodp.helpers
  (:require [clojure.test :as test]
            [cats.builtin]
            [cats.core :as m]
            [cats.monad.maybe :as maybe]
            [com.rpl.specter :as specter]
            [potemkin :as potemkin]))

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
  (specter/recursive-path [] p
                          (specter/cond-path seq? specter/STAY
                                             coll? [specter/ALL p]
                                             :else specter/STOP)))

(def quote-seq
  (partial specter/transform* Seqs quote-expr))

;This definition results in an error.
;(def quote-seq
;  (partial riddley/walk-exprs seq? quote-expr))
;
;((build (partial specter/transform* :a) (constantly inc) identity) {:a 0})
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

(defmacro defpfmethod
  [multifn dispatch-val f]
  `(defmethod ~multifn ~dispatch-val
     [& x#]
     (apply ~f x#)))

(defmacro defmulti-identity
  [mm-name]
  `(defmulti ~mm-name identity))

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
