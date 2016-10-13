(ns gof.jdpe.visitor
  (:require [flatland.ordered.map :refer [ordered-map]]))

(def engine
  (ordered-map :camshaft 1
               :piston 1
               :spark-plug 4))

(defn- diagnose-kind
  [[k v]]
  (let [part-name (name k)]
    (if (= v 1)
      (str "Diagnosing the " part-name)
      (->> (str "Diagnosing a " part-name)
           (repeat v)))))

(def diagnose
  (comp flatten
        (partial map diagnose-kind)))

(diagnose engine)
