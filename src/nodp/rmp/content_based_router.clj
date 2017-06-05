(ns nodp.rmp.content-based-router
  (:require [clojure.string :as str]
            [help.core :as help]
            [nodp.rmp.helpers :as rmp-helpers]))

(def split-dot
  (partial (help/flip str/split) #"\."))

(def get-super-kind
  (comp first
        split-dot
        :kind
        first))

(defmulti handle-items get-super-kind)

(help/defpfmethod handle-items "ABC"
                  rmp-helpers/handle-items)

(help/defpfmethod handle-items "XYZ"
                  rmp-helpers/handle-items)

(def handle
  (comp (partial map handle-items)
        vector))

(handle rmp-helpers/a-items rmp-helpers/x-items)
