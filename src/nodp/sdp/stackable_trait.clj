(ns nodp.sdp.stackable-trait
  (:require [clojure.string :as str]))

(def prefix
  (partial str "Writing the following data: "))

(def get-variations
  (comp (partial map prefix)
        (juxt (comp str/upper-case
                    str/capitalize)
              (comp str/capitalize
                    str/lower-case)
              (comp str/capitalize
                    str/upper-case
                    str/lower-case)
              (comp str/capitalize
                    str/lower-case
                    str/upper-case))))

(get-variations "we like learning scala!")
