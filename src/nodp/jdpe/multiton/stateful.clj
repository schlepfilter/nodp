(ns nodp.jdpe.multiton
  (:require [com.rpl.specter :as specter]
            [nodp.helpers :as helpers]))

(def generator
  (atom {:engine  0
         :vehicle 0}))

(defn- get-set-next-serial!
  [k]
  (->> (partial specter/transform* k inc)
       (swap! generator)))

(defn- label
  [k]
  (str "next " (name k) ":"))

(def print-next-serial
  (helpers/build println
                 label
                 get-set-next-serial!))

(dotimes [_ 3]
  (print-next-serial :engine))

(dotimes [_ 3]
  (print-next-serial :vehicle))

