(ns nodp.jdpe.multiton
  (:require [nodp.helpers :as helpers]))

(defmacro defgenerator
  [generator-name]
  `(def ~generator-name
     (atom 0)))

(defmacro defgenerators
  ([])
  ([generator-name & more]
   `(do (defgenerator ~generator-name)
        (defgenerators ~@more))))

(defgenerators engine vehicle)

(defn- get-next-serial
  [generator]
  (swap! generator inc))

(defn- get-description
  [generator-name]
  (str "next " generator-name ":"))

(def print-next-serial
  (helpers/build println
                 (comp get-description
                       :generator-name)
                 (comp get-next-serial
                       :generator)))

(dotimes [_ 3]
  (print-next-serial {:generator      engine
                      :generator-name "engine"}))

(dotimes [_ 3]
  (print-next-serial {:generator      vehicle
                      :generator-name "vehicle"}))
