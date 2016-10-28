(ns nodp.eip.splitter.concurrent
  (:require [nodp.eip.splitter.core :as core]))

(pmap core/handle-item core/items)
