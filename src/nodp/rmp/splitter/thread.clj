(ns nodp.rmp.splitter.thread
  (:require [nodp.rmp.splitter.core :as core]))

(pmap core/handle-item core/items)
