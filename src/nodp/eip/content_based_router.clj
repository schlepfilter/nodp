(ns nodp.eip.splitter.content-based-router
  (:require [nodp.eip.helpers :as eip-helpers]
            [clojure.string :as str]
            [nodp.helpers :as helpers]))

(def split-dot
  (partial (helpers/flip str/split) #"\."))

(def get-super-kind
  (comp first split-dot :kind first))

(defmulti handle-items get-super-kind)

(defmethod handle-items "ABC"
  [items]
  (eip-helpers/handle-items items))

(defmethod handle-items "XYZ"
  [items]
  (eip-helpers/handle-items items))

(map handle-items [eip-helpers/a-items eip-helpers/x-items])
