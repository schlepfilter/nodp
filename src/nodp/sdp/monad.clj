(ns nodp.sdp.monad
  (:require [cats.builtin]
            [cats.core :as m]))

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

(defn- multiply
  [& vs]
  (apply ap * vs))

(multiply [1 2 3 4] [5 6 7 8])
