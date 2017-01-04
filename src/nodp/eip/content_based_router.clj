(ns nodp.eip.content-based-router
  (:require [clojure.string :as str]
            [nodp.helpers :as helpers]
            [nodp.eip.helpers :as eip-helpers]))

(def split-dot
  (partial (helpers/flip str/split) #"\."))

(def get-super-kind
  (comp first
        split-dot
        :kind
        first))

(defmulti handle-items get-super-kind)

(helpers/defpfmethod handle-items "ABC"
                     eip-helpers/handle-items)

(helpers/defpfmethod handle-items "XYZ"
                     eip-helpers/handle-items)

(def handle
  (comp (partial map handle-items)
        vector))

(handle eip-helpers/a-items eip-helpers/x-items)
