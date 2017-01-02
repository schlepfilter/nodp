(ns nodp.eip.splitter.thread
  (:require [nodp.eip.splitter.core :as core]))

(pmap core/handle-item core/items)
