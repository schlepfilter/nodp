(ns nodp.rmp.competing-consumers.thread
  (:require [nodp.helpers :as helpers]
            [nodp.rmp.competing-consumers.core :as core]))

(defn- consume
  [work-item]
  (str (helpers/get-thread-name) " for " work-item))

;This is less readable.
;(defn- instantiate
;  [f]
;  (fn [& _] (f)))
;
;(def consume
;  (comp str/join
;        (juxt (instantiate helpers/get-thread-name)
;              (constantly " for "))))

(def consume-items
  (partial pmap consume))

(consume-items core/work-items)
