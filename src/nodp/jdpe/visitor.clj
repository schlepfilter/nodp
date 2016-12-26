(ns nodp.jdpe.visitor
  (:require [clojure.string :as str]
            [flatland.ordered.map :refer [ordered-map]]))

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
  (->> (if (= v 1)
         "the"
         "a singular")
       ((->> k
             name
             (make-get-sentence verb)))
       (repeat v)))

(def get-diagnoses
  (comp flatten
        (partial map get-diagnosis)))

(get-diagnoses engine)

(defn- get-count
  [[k v]]
  (str v " " (name k) "(s)"))

(def get-counts
  (comp (partial str/join ", ")
        (partial map get-count)))

(get-counts engine)
