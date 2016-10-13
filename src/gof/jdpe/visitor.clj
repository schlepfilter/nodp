(ns gof.jdpe.visitor
  (:require [clojure.string :as str]
            [flatland.ordered.map :refer [ordered-map]]
            [gof.helpers :as helpers]))

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
  (let [part-name (name k)
        get-sentence (make-get-sentence verb part-name)]
    (if (= v 1)
      (get-sentence "the")
      (->> (get-sentence "a singular")
           (repeat v)))))

(def get-diagnoses
  (comp flatten
        (partial map get-diagnosis)))

(def diagnose
  (comp helpers/printall get-diagnoses))

(diagnose engine)

(defn- get-count
  [[k v]]
  (str v " " (name k) "(s)"))

(def get-counts
  (comp (partial str/join ", ")
        (partial map get-count)))

(def count-parts
  (comp println get-counts))

(count-parts engine)

