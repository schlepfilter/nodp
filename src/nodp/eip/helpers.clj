(ns nodp.eip.helpers
  (:require [clojure.string :as str]))

(defn handle-items
  [items]
  (if (not-empty items)
    (->> items
         str/join
         (str "handling "))))


