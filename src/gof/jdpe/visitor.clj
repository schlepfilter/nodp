(ns gof.jdpe.visitor
  (:require [flatland.ordered.map :refer [ordered-map]]))

(def engine
  (ordered-map :camshaft 1
               :piston 1
               :spark-plug 4))

(defn- diagnose
  [[k v]]
  (if (= v 1)
    (str "Diagnosing the " (name k))
    (repeat v (str "Diagnosing a " (name k)))))

(flatten (map diagnose engine))
