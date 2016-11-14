(ns nodp.sdp.stackable-trait
  (:require [clojure.string :as str]
            [nodp.helpers :as helpers]))

(def prefix
  (partial str "Writing the following data: "))

(def get-arguments
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

(def printall
  (comp helpers/printall
        get-arguments))

(printall "we like learning scala!")
