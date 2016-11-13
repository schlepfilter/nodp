(ns nodp.jdpe.visitor
  (:require [clojure.string :as str]
            [flatland.ordered.map :refer [ordered-map]]
            [nodp.helpers :as helpers]))

(def engine
  (ordered-map :camshaft 1
               :piston 1
               :spark-plug 4))

(def verb
  "Diagnosing")

(defn- make-get-sentence
  [verb part-name]
  (fn [qualifier]
    (str/join " " [verb qualifier part-name])))

(defn- get-diagnosis
  [[k v]]
  (let [get-sentence (->> k
                          name
                          (make-get-sentence verb))]
    (if (= v 1)
      (get-sentence "the")
      (->> (get-sentence "a singular")
           (repeat v)))))

(def get-diagnoses
  (comp flatten
        (partial map get-diagnosis)))

(defn- get-count
  [[k v]]
  (str v " " (name k) "(s)"))

(def get-counts
  (comp (partial str/join ", ")
        (partial map get-count)))

(def get-arguments
  (helpers/build concat
                 get-diagnoses
                 (comp vector
                       get-counts)))

(def printall
  (comp helpers/printall
        get-arguments))

(printall engine)
