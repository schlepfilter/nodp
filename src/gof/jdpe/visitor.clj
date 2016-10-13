(ns gof.jdpe.visitor
  (:require [flatland.ordered.map :refer [ordered-map]]
            [clojure.string :as str]
            [gof.helpers :as helpers]))

(def engine
  (ordered-map :camshaft 1
               :piston 1
               :spark-plug 4))

(defn- get-diagnosis
  [[k v]]
  (let [part-name (name k)]
    (if (= v 1)
      (str "Diagnosing the " part-name)
      (->> (str "Diagnosing a " part-name)
           (repeat v)))))

(def get-diagnoses
  (comp flatten
        (partial map get-diagnosis)))

(def diagnose
  (comp helpers/printall
        get-diagnoses))

(diagnose engine)

(defn- count-kind
  [[k v]]
  (str v " " (name k) "(s)"))

(def count-all
  (comp (partial str/join ", ")
        (partial map count-kind)))

(count-all engine)

