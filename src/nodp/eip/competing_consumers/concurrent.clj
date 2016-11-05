(ns nodp.eip.competing-consumers.concurrent
  (:require [nodp.helpers :as helpers]
            [nodp.eip.competing-consumers.core :as core]))

(defn- consume
  [work-item]
  (str (helpers/get-thread-name) " for " work-item))

;This is less readable
;(defn- instantiate
;  [f]
;  (fn [] (f)))
;
;(def consume
;  (comp str/join
;        (juxt (instantiate helpers/get-thread-name)
;              (constantly " for "))))

(def printall
  (comp helpers/printall
        (partial pmap consume)))

(printall core/work-items)
