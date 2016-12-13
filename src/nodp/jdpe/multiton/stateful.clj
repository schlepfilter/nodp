(ns nodp.jdpe.stateful
  (:require [nodp.helpers :as helpers]))

(defmacro defgenerator
  [generator-name]
  `(def ~generator-name
     (atom 0)))

(helpers/make-defmacro defgenerators defgenerator)

(defgenerators engine vehicle)

(defn- get-next-serial
  [generator]
  (swap! generator inc))

(defn- label
  [generator-name]
  (str "next " generator-name ":"))

(def print-next-serial
  (helpers/build println
                 (comp label
                       :generator-name)
                 (comp get-next-serial
                       :generator)))

(dotimes [_ 3]
  (print-next-serial {:generator      engine
                      :generator-name "engine"}))

(dotimes [_ 3]
  (print-next-serial {:generator      vehicle
                      :generator-name "vehicle"}))
