(ns gof.jdpe.abstract-factory
  (:require [clojure.string :as str]))

(def description
  {:body    "Body shell parts"
   :chassis "Chassis parts"
   :window  "Window glassware"})

(defn make-get-parts
  [vehicle]
  (fn [part]
    (str/join " " [(part description) "for a" (name vehicle)])))

(def get-car-parts
  (make-get-parts :car))

(get-car-parts :body)
(get-car-parts :chassis)
(get-car-parts :window)
