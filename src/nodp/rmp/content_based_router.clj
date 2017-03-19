(ns nodp.rmp.content-based-router
  (:require [clojure.string :as str]
            [nodp.helpers :as helpers]
            [nodp.rmp.helpers :as rmp-helpers]))

(def split-dot
  (partial (helpers/flip str/split) #"\."))

(def get-super-kind
  (comp first
        split-dot
        :kind
        first))

(defmulti handle-items get-super-kind)

(helpers/defpfmethod handle-items "ABC"
                     rmp-helpers/handle-items)

(helpers/defpfmethod handle-items "XYZ"
                     rmp-helpers/handle-items)

(def handle
  (comp (partial map handle-items)
        vector))

(handle rmp-helpers/a-items rmp-helpers/x-items)
