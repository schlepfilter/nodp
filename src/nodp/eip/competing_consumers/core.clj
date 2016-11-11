(ns nodp.eip.competing-consumers.core
  (:require [nodp.helpers :as helpers]))

(def get-work-item
  (partial str "WorkItem"))

(def get-work-items
  (comp (partial map get-work-item)
        (partial range 1)))

(def work-items
  (get-work-items 101))

(def consume
  (partial str (helpers/get-thread-name) " for "))

(def printall
  (comp helpers/printall
        (partial map consume)))

(printall work-items)
