(ns nodp.rmp.content-based-router
  (:require [aid.core :as aid]
            [clojure.string :as str]
            [nodp.rmp.helpers :as rmp-helpers]))

(def split-dot
  (partial (aid/flip str/split) #"\."))

(def get-super-kind
  (comp first
        split-dot
        :kind
        first))

(defmulti handle-items get-super-kind)

(aid/defpfmethod handle-items "ABC"
                 rmp-helpers/handle-items)

(aid/defpfmethod handle-items "XYZ"
                 rmp-helpers/handle-items)

(def handle
  (comp (partial map handle-items)
        vector))

(handle rmp-helpers/a-items rmp-helpers/x-items)
