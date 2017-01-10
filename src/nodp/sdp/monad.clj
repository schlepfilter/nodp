(ns nodp.sdp.monad
  (:require [nodp.helpers :as helpers]
            [cats.core :as m]))

(defn ap
  ([f x]
   (m/<$> f x))
  ([f x & more]
   (apply m/<*>
          (m/<$> (helpers/curry f
                                (-> more
                                    count
                                    inc))
                 x)
          more)))

(defn- multiply
  [& vs]
  (apply ap * vs))

(def prefix
  (partial str "The result is: "))

(def get-results
  (comp prefix
        multiply))

(get-results [1 2 3 4] [5 6 7 8])
